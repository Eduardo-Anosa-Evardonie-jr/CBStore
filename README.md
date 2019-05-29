## Summary
Stores framework provides a unified API for storing, retrieving and observing Key-Value pairs on iOS and Android.

## Rationale
Deciding where to store data, how to transform it, how to observe it, or where to save the key requires additional decision making and manual work. This framework elevates all that by providing a simple consistent API across both platforms.

## Usage
First we need to define a `StoreKey`. A `StoreKey` defines how, what and where to store the value.

On iOS:
```swift
// Add an extension on StoreKeys
extension StoreKeys {
   // This will store a String in UserDefaults
   static let userID = UserDefaultsStoreKey<String>("userID")
   
   // This will cache a Boolean in memory.
   static let isPillHidden = MemoryStoreKey<Bool>("isPillHidden")
   
   // This will securily store a User object in Keychain
   static let user = KeychainStoreKey<User>("user")
}
```

On Android
```kotlin
// We cannot add a static property to an existing extension in Kotlin. Instead,
// create logical groupings for keys

object UserKeys {
   // This will store a String in Android SharedPreferences
   val userId = SharedPrefsStoreKey(id = "userID", clazz = String::class.java)
   
   // This will encrypt & store a User object in SharedPreferences
   val user = EncryptedSharedPrefsStoreKey(id = "user", clazz = User::class.java)
}

object WalletKeys {
   // This will cache a Boolean in memory.
   val isPillHidden = MemoryStoreKey(id = "isPillHidden", clazz = Boolean::class.java)
}
```

Once the key is defined, the store can be accessed, modifie, or observed as follows:

On iOS
```swift
// Get operation
let userID = store.get(.userID)
let user = store.get(.user)


// Set operation
store.set(.userID, "420")
store.set(.user, User(id: 123, name: "Adam"))

// Observe operation
store.observe(.isPillHidden)
    .subscribe(onNext: { value in
        // value is of type Bool
    })
```

On Android:
```kotlin
// Get operation
val userID = store.get(UserKeys.userId)
val user = store.get(UserKeys.user)

// Set operation
store.set(UserKeys.userId, "420")
store.set(UserKeys.user, User(id = 123, name = "Adam"))

// Observe operation
store.observe(WalletKeys.isPillHidden)
    .subscribe { value -> 
        // value is of type Boolean
    }
```