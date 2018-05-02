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
	section("Select what button you want for each mode..."){
        input "myDisarmButton", "capability.momentary", title: "What Button will disarm the alarm?", required: false, multiple: false
        input "myArmStay", "capability.momentary", title: "What button will put the alarm in Armed/Stay?", required: false, multiple: false
        input "myArmAway", "capability.momentary", title: "What button will put the alarm in Armed/Away?", required: false, multiple: false
	}
/*    section("What mode do you want to set. 1 = Disarmed, 2 = Armed/Stay, 3 = Armed/Away..."){
		input "alarmMode", "number", range: "1..3", title: "What Mode do you want to change into", required: true, defaultValue: 1
	}
*/
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
    subscribe(myDisarmButton, "momentary.pushed", disarmHandler)
    subscribe(myArmStay, "momentary.pushed", armstayHandler)
    subscribe(myArmAway, "momentary.pushed", armawayHandler)
}

def disarmHandler(evt) {
      log.debug "Disarming alarm"
      panel?.disarm()
	}

def armstayHandler(evt) {
       log.debug "Changeing alarm to Alarm/Stay"
       panel?.armStay(armedStay)
	}
    
def armawayHandler(evt) {
       log.debug "Changeing alarm to Alarm/Stay"
       panel?.armStay(armedStay)
	   }