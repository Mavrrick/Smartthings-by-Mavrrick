/**
 *  ADT Smoke Alert
 *
 *  Copyright 2018 CRAIG KING
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "ADT Smoke Alert",
    namespace: "Mavrrick",
    author: "CRAIG KING",
    description: "Smartthing ADT tools for additional functions ",
    category: "Safety & Security",
    parent: "Mavrrick:ADT Tools",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/* 
* 4/29/2018 v1.0.1
* Add Light Action to allow for Flashing and turing on lights
*
* Initial release v1.0.0
* Trigger action based on ADT Alarm. This is intial release of child app
*/
preferences {
	    section("Use these devices when ADT Alarm is triggered"){
			input "smoke", "capability.smokeDetector", title: "Look for ADT Activity on these smoke detectors", required: true, multiple: true
		}
		section("Action to trigger when ADT Alarm is triggered"){
        	input "alarms", "capability.alarm", title: "Which Alarm(s) to trigger when ADT alarm goes off", multiple: true, required: false
        paragraph "Valid alarm types are 1= Siren, 2=Strobe, and 3=Both. All other numberical valudes wil be ignored"
        	input "alarmtype", "number", title: "What type of alarm do you want to trigger", required: false, defaultValue: 3
        paragraph "Valid Light actions are are 1 = None, 2 = Turn on lights, 3 = Flash Lights and 4 = Both. All other numberical valudes wil be ignored"
        	input "lightaction", "number", title: "What type of light action do you want to trigger", required: true, defaultValue: 1
        paragraph "If you choose Light action 4 do not select the same lights in both values"
        	input "switches2", "capability.switch", title: "Turn these lights on if Light action is set to 2 or 4", multiple: true, required: false
        	input "switches", "capability.switch", title: "Flash these lights (optional) If Light action is set to 3 or 4", multiple: true, required: false
    	}
    	section("Flashing Lights setup (Optional)"){
			input "onFor", "number", title: "On for (default 5000)", required: false
			input "offFor", "number", title: "Off for (default 5000)", required: false
        	input "numFlashes", "number", title: "This number of times (default 3)", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
        subscribe(location, "alarm", alarmHandler)
}

def devices = " "

def alarmHandler(evt) {

switch (evt.value)
	{
    case "CLEARED":
    log.debug "Notify got alarm clear event ${evt}"
    alarms?.off()
    	break 
    case "siren":
    log.debug "siren turned on"
    	break
    case "strobe":
    log.debug "Strobe is turned on"
    	break
    case "both":
    log.debug "Siren and Strobe turned on"
    	break
    case "off":
    log.debug "Siren and Strobe turned off"
    	break
    default:
	log.debug "Notify got alarm event ${evt}"
    log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"
        log.debug "The event id to be compared is ${evt.value}"  
		def devices = settings.smoke
        log.debug "These devices were found ${devices.id} are being reviewed."
    	devices.findAll { it.id == evt.value } .each { 
        log.debug "Found device: ID: ${it.id}, Label: ${it.label}, Name: ${it.name}, Is water Event"
        switch (alarmtype.value)
        	{
            	case 1 :
                	log.debug "Alarm type ${alarmtype.value} detected. Turning on siren"
                    alarms?.siren()
                    break
                case 2 :
                	log.debug "Alarm type ${alarmtype.value} detected. Turning on strobe"
                    alarms?.strobe()
                    break
                case 3 :
                	log.debug "Alarm type ${alarmtype.value} detected. Turning on Siren and Strobe"
                    alarms?.both()
                    break
                default:
					log.debug "Ignoring unexpected alarmtype mode."
        			log.debug "Alarm type ${alarmtype.value} detected"
                    break
                    }
 		switch (lightaction.value)
        	{
            	case 1 :
                	log.debug "Light action ${lightaction.value} detected. No Light Action"                    
                    break
                case 2 :
                	log.debug "Light action ${lightaction.value} detected. Turning on selected lights"
                    switches2?.on()
                    break
                case 3 :
                	log.debug "Light Action ${lightaction.value} detected. Flashing Selected lights"                    
                    flashLights()
                    break
                case 4 :
                	log.debug "Light Action ${lightaction.value} detected. Flash and turning on selected lights"
                    switches2?.on()
                    flashLights()
                    break
                default:
					log.debug "Ignoring unexpected Light Action type."
        			log.debug "Light Action ${lightaction.value} detected"
                    break
			} 		
 		}
		break
}
}

def continueFlashing()
{
	unschedule()
	if (state.alarmActive) {
		flashLights(10)
		schedule(util.cronExpression(now() + 10000), "continueFlashing")
	}
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 5000
	def offFor = offFor ?: 5000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}