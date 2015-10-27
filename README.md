# A3Droid Test Applications: Media Content Sharing Sender

Currently alljoyn framework only supports wi-fi as a stable connection method. Bluetooth has been disabled and wi-fi direct is still experimental. Make sure all devices are in the same wi-fi local area network.

## Required libraries

* Android v4 .jars

## Experiment steps

* Select one device to be the remote sender
* Define the *server address* witch will reply with the receiver group supervisor address
  * For simplicity purposes, no XMPP alike server has been implemented
  * The receiver group supervisor will also play the role of the server by replying RFS with its own address
  * The server address should be the same as the receiver group supervisor
    * Check among the [receiver group devices](https://github.com/danilomendonca/A3Droid_Test_MCS) witch has been elected the group supervisor
    * The supervisor, once created, prints a [A3Test_GROUP_ID_SupRole] log message
    * The follower, once created, prints a [A3Test_GROUP_ID_FolRole] log message
    * The supervisor IP can be checked at the advanced WI-FI screen in Android devices
* Click in the **Start Experiment** button
* Check the log messages at the application interface of the sender and the [receiver group devices](https://github.com/danilomendonca/A3Droid_Test_MCS)
* Click in the **Stop Experiment** button whenever you want to finish it
* Check the A3Droid_Mediasharing.txt file at the *Virtual external disk* root folder of the server device with all experiment measurements
