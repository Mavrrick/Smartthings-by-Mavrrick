/**
 *  ADT Tools
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
    iconUrl: "https://lh4.googleusercontent.com/-1dmLp--W0OE/AAAAAAAAAAI/AAAAAAAAEYU/BRuIXPPiOmI/s0-c-k-no-ns/photo.jpg",
    iconX2Url: "https://lh4.googleusercontent.com/-1dmLp--W0OE/AAAAAAAAAAI/AAAAAAAAEYU/BRuIXPPiOmI/s0-c-k-no-ns/photo.jpg",
    iconX3Url: "https://lh4.googleusercontent.com/-1dmLp--W0OE/AAAAAAAAAAI/AAAAAAAAEYU/BRuIXPPiOmI/s0-c-k-no-ns/photo.jpg",
    singleInstance: true)

/* 
*10/27/2018
*Removed some commented code for the camera integration
*
*7/20/2018
*Updates to UI and to include ADT Alert Any Sensor child app
*
*5/1/2018
*Added new child app to allow for changing the ADT Panel Alarm Mode from Smarthings
*
*4/27/2018
*Modified UI to use Child/parent Apps. New Child apps are required to trigger items from alarm.
*Added child app to trigger based on ADT Smoke alarm
*Corrected bug in Alarm Trigger that would cuase it fail when some sensors types were not configured
*
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
    // The parent app preferences are pretty simple: just use the app input for the child app.
    page(name: "mainPage", title: "Tools", install: true, uninstall: true,submitOnChange: true) {
    	section ("New Version is out "){
        paragraph "There is a new version of ADT Tools out. Version 1 is deprecated and all future updates will go to the new version. Please vist the release thread for more details."
         }
        section ("ADT Integration Apps"){
            app(name: "adtNotifier", appName: "ADT Notifier", namespace: "Mavrrick", title: "Create custom notification when alarm changes state", multiple: true)
            app(name: "adtModeChange", appName: "ADT Mode Change", namespace: "Mavrrick", title: "Allows changing alarm mode from smartapps", multiple: true)            
            }
        section ("Alarm Event Action Apps"){
            app(name: "adtContactAlarm", appName: "ADT Door or Window Alert", namespace: "Mavrrick", title: "Create new Door or Window triggered event action", multiple: true)
            app(name: "adtMotionAlarm", appName: "ADT Motion Alert", namespace: "Mavrrick", title: "Create new Motion triggered event action", multiple: true)
            app(name: "adtSmokeAlarm", appName: "ADT Smoke Alert", namespace: "Mavrrick", title: "Create new Smoke triggered event action", multiple: true)
            app(name: "adtWaterAlarm", appName: "ADT Water Alert", namespace: "Mavrrick", title: "Create new Water Leak triggered event action", multiple: true)
            app(name: "adtAnySensor", appName: "ADT Alert Any Sensor", namespace: "Mavrrick", title: "Allows Unmonitored Alarm action based on ADT status", multiple: true)            
		}
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
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}