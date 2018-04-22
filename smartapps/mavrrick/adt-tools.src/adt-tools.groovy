/**
 *  Notify If Alarm State Changes
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
    name: "ADT Tools",
    namespace: "Mavrrick",
    author: "CRAIG KING",
    description: "Smartthing ADT tools for additional functions ",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/* 
* 4/22/2018 v1.1.1
*Cleaned up user interface a bit
*Updated Message handeling to allow no message provided
*
*4/21/2018 v1.1
*Update app to allow for usage of alarm events. 
*Added ability to trigger alarm device in 3 modes.
*Added ability to flash selected lights.
*Updated various inputs to required.
*
* Initial release v1.0.0
*Allow custom messages when system changes alarm state between Armed/Stay, Armed/Away and disarmed.
*/
preferences {
	section("Set Message for each state"){
		input "messageDisarmed", "text", title: "Send this message if alarm changes to Disarmed", required: false
        input "messageArmedAway", "text", title: "Send this message if alarm changes to Armed/Away", required: false
        input "messageArmedStay", "text", title: "Send this message if alarm changes to Armed/Stay", required: false
	}
    section("Use these devices when ADT Alarm is triggered"){
		input "contact", "capability.contactSensor", title: "Look for ADT Activity on these contact sesors", required: false, multiple: true
	    input "water", "capability.waterSensor", title: "Look for ADT Activity on these water sesors", required: false, multiple: true
		input "motion", "capability.motionSensor", title: "Look for ADT Activity on these motion sesors", required: false, multiple: true
        input "alarms", "capability.alarm", title: "Which Alarm(s) to trigger when ADT alarm goes off", multiple: true, required: false
        input "alarmtype", "number", title: "What type of alarm do you want to trigger", required: false, defaultValue: 3, options: [
			1:"Siren",
			2:"Strobe",
			3:"Both",
		]
        paragraph "Valid alarm types are 1= Siren, 2=Strobe, and 3=Both. All other numberical valudes wil be ignored"
        input "switches", "capability.switch", title: "Flash these lights (optional) when alarm is triggered", multiple: true, required: false
    }
    section("Flashing Lights setup (Optional)"){
		input "onFor", "number", title: "On for (default 5000)", required: false
		input "offFor", "number", title: "Off for (default 5000)", required: false
        input "numFlashes", "number", title: "This number of times (default 3)", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Enter a phone number to get SMS", required: false
			paragraph "If outside the US please make sure to enter the proper country code"
			input "pushAndPhone", "enum", title: "Notify me via Push Notification", required: false, options: ["Yes", "No"]
		}
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
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
        subscribe(location, "securitySystemStatus", eventHandler)
        subscribe(location, "alarm", alarmHandler)
}

def msg = "" 
def devices = " "

def eventHandler(evt) {
	log.debug "Notify got evt ${evt}"
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		sendMessage(evt)
	}
}

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
    def devices = settings.water + settings.contact + settings.motion
    log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"
        log.debug "These devices were found ${devices.id} are being reviewed."
        log.debug "The event id to be compared is ${evt.value}"  
        devices.findAll { it.id == evt.value } .each { 
        log.debug "Found device: ID: ${it.id}, Label: ${it.label}, Name: ${it.name}"
        }
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
		flashLights()
		break
}
}

private sendMessage(evt) {

switch (evt.value)
    {
    case "armedAway":
    	def msg = messageArmedAway
           if ( msg == null ) {
        	log.debug "Message not configured. Skipping notification"
            }
        else {
        log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	Map options = [:]	

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone != 'No') {
				log.debug 'Sending push and SMS'
				options.method = 'both'
			} else {
				log.debug 'Sending SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone != 'No') {
			log.debug 'Sending push'
			options.method = 'push'
		} else {
			log.debug 'Sending nothing'
			options.method = 'none'
		}
		sendNotification(msg, options)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}
        break
    case "armedStay":
    	def msg = messageArmedStay
        if ( msg == null ) {
        	log.debug "Message not configured. Skipping notification"
            }
        else {
        log.debug "Case Armstay., '$msg'"
        log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	Map options = [:]	

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone != 'No') {
				log.debug 'Sending push and SMS'
				options.method = 'both'
			} else {
				log.debug 'Sending SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone != 'No') {
			log.debug 'Sending push'
			options.method = 'push'
		} else {
			log.debug 'Sending nothing'
			options.method = 'none'
		}
		sendNotification(msg, options)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}
        break
    case "disarmed":
    	def msg = messageDisarmed
        if ( msg == null ) {
        	log.debug "Message not configured. Skipping notification"
            }
        else {
        log.debug "Case disarmed., '$msg'"
        log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	Map options = [:]	

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone != 'No') {
				log.debug 'Sending push and SMS'
				options.method = 'both'
			} else {
				log.debug 'Sending SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone != 'No') {
			log.debug 'Sending push'
			options.method = 'push'
		} else {
			log.debug 'Sending nothing'
			options.method = 'none'
		}
		sendNotification(msg, options)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}
        break
    default:
		log.debug "Ignoring unexpected ADT alarm mode."
        log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"
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