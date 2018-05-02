/**
 *  ADT Mode Change
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
    name: "ADT Mode Change",
    namespace: "Mavrrick",
    author: "CRAIG KING",
    description: "ADT Child app to change modes.",
    category: "My Apps",
    parent: "Mavrrick:ADT Tools",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When any of the following devices trigger..."){
		input "mySwitch", "capability.switch", title: "Switch?", required: false, multiple: false
        input "myButton", "capability.button", title: "Button?", required: false, multiple: false
	}
    section("What mode do you want to set. 1 = Disarmed, 2 = Armed/Stay, 3 = Armed/Away..."){
		input "alarmMode", "number", range: "1..3", title: "What Mode do you want to change into", required: true, defaultValue: 1
	}
	section("Select your ADT Smart Panel..."){
		input "panel", "capability.battery", title: "ADT Panel?", required: true
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"


	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(mySwitch, "switch.on", switchOnHandler)   
    subscribe(myButton, "button.pushed", buttonHandler)
}

def switchOnHandler(evt) {
        switch (alarmMode.value)
        	{
            	case 1 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Disarming alarm"
                    panel?.disarm()
                    mySwitch?.off()
                    break
                case 2 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Changeing alarm to Alarm/Stay"
                    panel?.armStay(armedStay)
                    mySwitch?.off()
                    break
                case 3 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Changeing alarm to Alarm/Stay"
                    panel?.armAway(armedAway)
                    mySwitch?.off()
                    break
                default:
					log.debug "Ignoring unexpected alarmmode mode."
        			log.debug "Alarm mode ${alarmMode.value} detected"
                    break
                    }
              }


def buttonHandler(evt) {
        switch (alarmMode.value)
        	{
            	case 1 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Disarming alarm"
                    panel?.disarm()
                    break
                case 2 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Changeing alarm to Alarm/Stay"
                    panel?.armStay(armedStay)
                    break
                case 3 :
                	log.debug "Alarm mode ${alarmMode.value} detected. Changeing alarm to Alarm/Stay"
                    panel?.armAway(armedAway)
                    break
                default:
					log.debug "Ignoring unexpected alarmmode mode."
        			log.debug "Alarm mode ${alarmMode.value} detected"
                    break
                    }
              }