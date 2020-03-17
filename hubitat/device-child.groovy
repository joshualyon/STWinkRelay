/**
*  Wink Relay Child
*
*  Author: 
*    Adam Kempenich
*
*  Documentation:  [Does not exist, yet]
*
*  Changelog:
*    1.0 (Feb 17, 2020)
*        - Published Commit
*
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

metadata {
definition (
    name: "Wink Relay Child", 
    namespace: "joshualyon", 
    author: "Adam Kempenich",
    importUrl: "https://raw.githubusercontent.com/joshualyon/STWinkRelay/master/hubitat/device-child.groovy") {
    
        capability "Actuator"
		capability "Initialize"
        capability "Polling"
        capability "Refresh"
		capability "Sensor"
		capability "Switch"
	}
    preferences {  
         // No preferences for this device
    }
}

def on(){
    // Turn the device on
    
    // sendEvent is handled by parent device
    def deviceType = device.deviceNetworkId.split("-")
    if(deviceType.length == 2){
        // Type is Backlight

        parent.screenBacklightOn()
    }
    else if(deviceType.length == 3){
        // Device Type is Relay

        if(deviceType[2] == "1"){
            // Relay 1
            parent.relay1On()
        } else{
            // Relay 2
            parent.relay2On()
        }
    }   
}
def off(){
    // Turn the device on
    
    // sendEvent is handled by parent device
    def deviceType = device.deviceNetworkId.split("-")
    if(deviceType.length == 2){
        // Type is Backlight

        parent.screenBacklightOff()
    }
    else if(deviceType.length == 3){
        // Device Type is Relay

        if(deviceType[2] == "1"){
            // Relay 1
            parent.relay1Off()
        } else{
            // Relay 2
            parent.relay2Off()
        }
    }   
}

def initialize(){
    // Do nothing
    
}

def updated(){
    // Do nothing
    
}

def refresh(){
    // Request an info packet from the gateway
    
    parent.refresh()
}
