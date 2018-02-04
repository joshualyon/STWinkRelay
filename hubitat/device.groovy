/**
 *  Wink Relay Device Handler
 *
 *  Copyright 2017 Joshua Lyon
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
    definition (name: "Wink Relay", namespace: "joshualyon", author: "Josh Lyon") {
        capability "Switch"
        //attribute "switch" //on/off
        //command "on"
        //command "off"
        capability "Polling"
        //command "poll"
        capability "Refresh"
        //command "refresh"
        capability "Temperature Measurement"
        //attribute "temperature"
        capability "Motion Sensor"
        //attribute "motion" //active/inactive
        capability "Relative Humidity Measurement"
        //attribute "humidity"

        attribute "proximityRaw", "string"
        attribute "proximity", "number"
        
        attribute "relay1", "enum", ["on", "off"]
        command "relay1On"
        command "relay1Off"
        //command "relay1Toggle"

        attribute "relay2", "enum", ["on", "off"]
        command "relay2On"
        command "relay2Off"
        //command "relay2Toggle"

        attribute "screenBacklight", "enum", ["on", "off"]
        command "screenBacklightOn"
        command "screenBacklightOff"
        //command "screenBacklightToggle"

        attribute "topButton", "enum", ["on", "off"]
        attribute "bottomButton", "enum", ["on", "off"]
    }
}

def installed(){
    sendHubCommand( refresh() )
    //setupEventSubscription() - refresh includes this now
}
def updated(){
    sendHubCommand( refresh() )
    //setupEventSubscription() - refresh includes this now
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    log.debug "JSON: ${msg.json}"
    
    if(msg?.json?.Relay1){
        log.info "Relay 1: ${msg.json.Relay1}"
        //if (switch1) { switch1.sendEvent(name: "switch", value: msg.json.Relay1) }
        sendEvent(name: "relay1", value: msg.json.Relay1)
    }
    if(msg?.json?.Relay2){
        log.info "Relay 2: ${msg.json.Relay2}"
        //if (switch2) { switch2.sendEvent(name: "switch", value: msg.json.Relay2) }
        sendEvent(name: "relay2", value: msg.json.Relay2)
    }
    if(msg?.json?.Temperature){
        if(msg?.json?.isRaw){
            log.info "Temperature (Raw): ${msg.json.Temperature}"
            def temperature = roundValue( (msg.json.Temperature.toInteger() / 1000) * 1.8 + 32 )
            log.info "Temperature: ${temperature}"
            sendEvent(name: "temperature", value: temperature)
        }
        else{
            log.info "Temperature: ${msg.json.Temperature}"
            sendEvent(name: "temperature", value: roundValue(msg.json.Temperature))
        }
    }
    if(msg?.json?.Humidity){
        if(msg?.json?.isRaw){
            log.info "Humidity (Raw): ${msg.json.Humidity}"
            def humidity = roundValue(msg.json.Humidity.toInteger() / 1000)
            log.info "Humidity: ${humidity}"
            sendEvent(name: "humidity", value: humidity)
        }
        else{
            log.info "Humidity: ${msg.json.Humidity}"
            sendEvent(name: "Humidity", value: roundValue(msg.json.Humidity))
        }
    }
    if(msg?.json?.Proximity){
        if(msg?.json?.isRaw){
            log.info "Proximity (RAW): ${msg.json.Proximity}"
            def prox = parseProximity(msg.json.Proximity)
            log.info "Proximity: ${prox}"
            sendEvent(name: "proximityRaw", value: msg.json.Proximity)
            sendEvent(name: "proximity", value: prox)
        }
        else{
            log.info "Proximity: ${msg.json.Proximity}"
            sendEvent(name: "proximity", value: msg.json.Proximity)
        }
    }
    if(msg?.json?.LCDBacklight){
        log.info "LCD Backlight: ${msg.json.LCDBacklight}"
        sendEvent(name: "screenBacklight", value: msg.json.LCDBacklight)
    }
    if(msg?.json?.BottomButton){
        log.info "Bottom Button: ${msg.json.BottomButton}"
        sendEvent(name: "bottomButton", value: msg.json.BottomButton)
    }
    if(msg?.json?.TopButton){
        log.info "Top Button: ${msg.json.TopButton}"
        sendEvent(name: "topButton", value: msg.json.TopButton)
    }

    //if both relays are on and the switch isn't currently on, let's raise that value
    if((switch1.currentValue("switch") == "on" || switch2.currentValue("switch") == "on") && device.currentValue("switch") != "on"){
        sendEvent(name: "switch", value: "on")
    }
    //and same in reverse
    if(switch1.currentValue("switch") == "off" && switch2.currentValue("switch") == "off" && device.currentValue("switch") != "off"){
        sendEvent(name: "switch", value: "off")
    }
}

def roundValue(x){
	Math.round(x * 10) / 10
}

def parseProximity(proxRaw){
	//split on spaces and grab the first value
    proxRaw.split(" ")[0] as Integer
}

//for now, we'll just have these turn on both relays
//in the future, we plan on providing the ability to disable either relay via the Android app
def on(){
    def action = []
    action << relay1On()
    action << relay2On()
    return action
}
def off(){
    def action = []
    action << relay1Off()
    action << relay2Off()
    return action
}

//TODO: change actions to POST commands on the server and here
def relay1On(){
    httpGET("/relay/top/on")
}
def relay1Off(){
    httpGET("/relay/top/off")
}
def relay1Toggle(){} //TODO: implement relay1 toggle

def relay2On(){
    httpGET("/relay/bottom/on")
}
def relay2Off(){
    httpGET("/relay/bottom/off")
}
def relay2Toggle(){} //TODO: implement relay2 toggle

def screenBacklightOn(){} //TODO: implement screen backlight control
def screenBacklightOff(){}
def screenBacklightToggle(){}

//Individual commands for retrieving the status of the Wink Relay over HTTP
def retrieveRelay1(){ httpGET("/relay/top") }
def retrieveRelay2(){ httpGET("/relay/bottom")}
def retrieveTemperature(){ httpGET("/sensor/temperature/raw") }
def retrieveHumidity(){ httpGET("/sensor/humidity/raw") }
def retrieveProximity(){ httpGET("/sensor/proximity/raw") }
def retrieveScreenBacklight(){ httpGET("/lcd/backlight") }

def setupEventSubscription(){
    log.debug "Subscribing to events from Wink Relay"
    def result = new physicalgraph.device.HubAction(
            method: "POST",
            path: "/subscribe",
            headers: [
                    HOST: getHostAddress(),
                    CALLBACK: getCallBackAddress()
            ]
    )
    //log.debug "Request: ${result.requestId}"
    return result
}



def poll(){
    refresh()
}
def refresh(){
    //manually get the state of sensors and relays via HTTP calls
    def httpCalls = []
    httpCalls << retrieveRelay1()
    httpCalls << retrieveRelay2()
    httpCalls << retrieveTemperature()
    httpCalls << retrieveHumidity()
    httpCalls << retrieveProximity()
    httpCalls << retrieveScreenBacklight()
    httpCalls << setupEventSubscription()
    return httpCalls
}


def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
    refresh()
}

def httpGET(path) {
	def hostUri = hostAddress
    log.debug "Sending command ${path} to ${hostUri}"
    def result = new physicalgraph.device.HubAction(
            method: "GET",
            path: path,
            headers: [
                    HOST: hostUri
            ]
    )
    //log.debug "Request: ${result.requestId}"
    return result
}




// gets the address of the Hub
private getCallBackAddress() {
    return "http://" + device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}