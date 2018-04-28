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
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/* 
*4/27/2018
*Modified UI to use Child/parent Apps. New ADT notifier and ADT Alarm Action child apps required
*Added ability for Alarm Action Child app to trigger based on ADT Smoke alarm
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
        section {
            app(name: "adtNotifier", appName: "ADT Notifier", namespace: "Mavrrick", title: "Create custom notification when alarm changes state", multiple: true)
            app(name: "adtTriggeredAlarm", appName: "ADT Alarm Action", namespace: "Mavrrick", title: "Create new alarmed triggered event action", multiple: true)
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