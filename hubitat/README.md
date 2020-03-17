Hubitat variation of the STWinkRelay integration. Requires the associated [STWinkRelay.apk](http://wink.boshdirect.com/updates/files/STWinkRelay_0-2-1a.apk) to be installed and the Wink Relay to be rooted.

> Note that @adamkempenich added support for Child Devices in the Wink Relay, so you should now install both the `device.groovy` and `device-child.groovy` device drivers into Hubitat before running the discovery. 
>
> If you already had an existing device, be sure to update your `device.groovy`, install the new `device-child.groovy` and run the **configure** command on your existing device so it will add the child devices. 

1. Install the `smartapp.groovy` under **Apps Code** in Hubitat. 
1. Install the `device.groovy` under **Drivers Code** in Hubitat
1. Install the `device-child.groovy` under **Drivers Code** in Hubitat
1. Follow the remainder of the instructions from the Hubitat community post:  
   https://community.hubitat.com/t/beta-wink-relay-lan-integration/272?u=josh
