# Rethink DNS + Firewall for Android
## _Custom Content Provider_

A content provider that manages access to a Rethink data, intended to be used by other applications.

## Features

- Application Info
- Blocklist Info

### Application Info
A Provider to present all application information available within Rethink to external applications.

##### Retrieving data from Provider

To retrieve the application data, the external application application needs "access permission" for the provider. As mentioned in [here](https://github.com/celzero/rethink-app/blob/main/app/src/headless/AndroidManifest.xml#L6)

The data can be retrieved either by setting up an observer or by building a request to retrieve the data from the provider. This [link](https://github.com/hussainmohd-a/rethink-app-inter-op/blob/main/app/src/main/java/com/celzero/interop/AppsActivity.kt) provides an example of an observer who observes and loads the application information in a recycler view.

##### Inserting, updating, and deleting application Info
To insert app information into Rethink, call the `ContentResolver.insert()` method. It adds a new entry to the database and returns a content URI for that line. The content values will have the following parameters

| Parameter | Description |
| ------ | ------ |
| uid | Application UID (INTEGER) |
| packageName | Package name (STRING)|
| appName | Application name (STRING)|
| isSystemApp | 0, 1 (1 if System app else 0) (INTEGER)|
| firewallStatus | Various firewall statuses (0 - Allow, 1 - Block) (INTEGER) |
| metered | 0,1,2 (0 - BOTH Metered and Unmetered, 1 - Unmetered, 2 - Metered)(INTEGER)|
| backgroundAllowed | 0 (unused) (INTEGER) |
| screenOffAllowed | 0 (unused) (INTEGER)|
| wifiDataUsed | 0 (unused) (INTEGER)|
| mobbileDataUsed | 0 (unused) (INTEGER)|

> **metered parameter is used when the firewallStatus is set to `BLOCK`

Similarly ```ContentResolver.Delete() and ContentResolver.Update()```
methods can be used.

The [RethinkInterOp](https://github.com/hussainmohd-a/rethink-app-inter-op/blob/main/app/src/main/java/com/celzero/interop/AppsCrudActivity.kt) demo app has sample code for above mentioned functions.

### Blocklist Info
A Provider to present all information available on the blocklist in Rethink to external apps.

##### Retrieving data from Provider

To retrieve the blocklist data, the external application application needs "access permission" for the provider. As mentioned in [here](https://github.com/celzero/rethink-app/blob/main/app/src/headless/AndroidManifest.xml#L6)

The data can be retrieved either by setting up an observer or by building a request to retrieve the data from the provider. This [link](https://github.com/hussainmohd-a/rethink-app-inter-op/blob/main/app/src/main/java/com/celzero/interop/BlocklistsActivity.kt) provides an example of an observer who observes and loads the blocklist data into a recycler view.

##### Inserting, updating, and deleting application Info

**Insert** and **delete** operations for the blocklists are not supported yet. Will be added in future releases. 

To update the blocklists details use `ContentResolver.update()` method. This method inserts a new row into the provider and returns a content URI for that row. The content values will have the following parameters

Also, use `ContentResolver.call()` method to retrieve the current blocklist stamp.

| Parameter | Description |
| ------ | ------ |
| value | unique id for blocklist (INTEGER) |
| vname | Blocklist name (STRING) |
| uname | Blocklist information (STRING) |
| group | Group name (STRING) |
| subGroup | Subgroup name (STRING) |
| url | Source link of blocklist (STRING) |
| show | 0 (unused) (INTEGER) |
| entries | Number of domain entries in blocklists (INTEGER) |
| pack | 0 (STRING) |
| isSelected | 0-unselected or 1-selected (INTEGER) |

Sample code: [Blocklists sample](https://github.com/hussainmohd-a/rethink-app-inter-op/blob/main/app/src/main/java/com/celzero/interop/BlocklistsCrudActivity.kt)

```
// URI to get the stamp
private val GET_STAMP = "content://${AUTHORITY}/blocklists/stamp/get"
// retrieves the current blocklist stamp from Rethink
val bundle = contentResolver.call(URI_BLOCKLISTS, GET_STAMP, null, null)
val stamp = bundle?.getString("stamp") ?: ""
```
