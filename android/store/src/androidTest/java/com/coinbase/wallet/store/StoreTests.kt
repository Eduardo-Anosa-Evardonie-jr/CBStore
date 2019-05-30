package com.coinbase.wallet.store

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.coinbase.wallet.store.models.EncryptedSharedPrefsStoreKey
import com.coinbase.wallet.store.models.MemoryStoreKey
import com.coinbase.wallet.store.models.SharedPrefsStoreKey
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StoreTests {
    @Test
    fun testStore() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val store = Store(appContext)
        val stringKey = SharedPrefsStoreKey(id = "string_key", uuid = "id", clazz = String::class.java)
        val boolKey = SharedPrefsStoreKey(id = "bool_key", uuid = "id", clazz = Boolean::class.java)
        val complexObjectKey = SharedPrefsStoreKey(id = "complex_object", clazz = MockComplexObject::class.java)
        val intKey = SharedPrefsStoreKey(id = "intKey", clazz = Int::class.java)
        val floatKey = SharedPrefsStoreKey(id = "floatKey", clazz = Float::class.java)
        val longKey = SharedPrefsStoreKey(id = "longKey", clazz = Long::class.java)
        val expected = "Hello Android CBStore"
        val expectedComplex = MockComplexObject(name = "hish", age = 37, wallets = listOf("hello", "world"))
        val expectedInt = 12345
        val expectedFloat = 1420f
        val expectedLong = 650022L

        store.set(intKey, expectedInt)
        store.set(floatKey, expectedFloat)
        store.set(longKey, expectedLong)
        store.set(stringKey, expected)
        store.set(boolKey, false)
        store.set(complexObjectKey, expectedComplex)
        store.set(TestKeys.computedKey(uuid = "random"), "hello")
        store.set(TestKeys.activeUser, "random")

        assertEquals(expectedInt, store.get(intKey))
        assertEquals(expectedFloat, store.get(floatKey))
        assertEquals(expectedLong, store.get(longKey))
        assertEquals(expected, store.get(stringKey))
        assertEquals(false, store.get(boolKey))
        assertEquals(expectedComplex, store.get(complexObjectKey))
        assertEquals("hello", store.get(TestKeys.computedKey(uuid = "random")))
    }

    @Test
    fun testMemory() {
        val expected = "Memory string goes here"
        val appContext = InstrumentationRegistry.getTargetContext()
        val store = Store(appContext)

        store.set(TestKeys.memoryString, expected)

        assertEquals(expected, store.get(TestKeys.memoryString))
    }

    @Test
    fun testObserver() {
        val expected = "Testing observer"
        val appContext = InstrumentationRegistry.getTargetContext()
        val store = Store(appContext)
        val latchDown = CountDownLatch(1)
        var actual = ""

        GlobalScope.launch {
            store.observe(TestKeys.memoryString)
                .filter { it.element != null }
                .timeout(6, TimeUnit.SECONDS)
                .subscribe({
                    actual = it.element ?: throw AssertionError("No element found")
                    latchDown.countDown()
                }, { latchDown.countDown() })
        }

        store.set(TestKeys.memoryString, expected)
        latchDown.await()

        assertEquals(expected, actual)
    }

    @Test
    fun encryptStringStoreKeyValue() {
        val expectedText = "Bitcoin + Ethereum"
        val store = Store(InstrumentationRegistry.getTargetContext())

        store.set(TestKeys.encryptedString, expectedText)

        val actual = store.get(TestKeys.encryptedString)

        assertEquals(expectedText, actual)
    }

    @Test
    fun encryptComplexObjectStoreKeyValue() {
        val expected = MockComplexObject(name = "hish", age = 37, wallets = listOf("1234", "2345"))
        val store = Store(InstrumentationRegistry.getTargetContext())

        store.set(TestKeys.encryptedComplexObject, expected)

        val actual = store.get(TestKeys.encryptedComplexObject)

        if (actual == null) {
            Assert.fail("Unable to get encrypted complex object")
            return
        }

        assertEquals(expected.name, actual.name)
        assertEquals(expected.age, actual.age)
        assertEquals(expected.wallets, actual.wallets)
    }

    @Test
    fun encryptArrayStoreKeyValue() {
        val expected = arrayOf("Bitcoin", "Ethereum")
        val store = Store(InstrumentationRegistry.getTargetContext())

        store.set(TestKeys.encryptedArray, expected)

        val actual = store.get(TestKeys.encryptedArray)

        assertArrayEquals(expected, actual)
    }

    @Test
    fun encryptComplexObjectArrayStoreKeyValue() {
        val expected = arrayOf(
            MockComplexObject(name = "hish", age = 37, wallets = listOf("1234", "2345")),
            MockComplexObject(name = "aya", age = 3, wallets = listOf("333"))
        )

        val store = Store(InstrumentationRegistry.getTargetContext())

        store.set(TestKeys.encryptedComplexObjectArray, expected)

        val actual = store.get(TestKeys.encryptedComplexObjectArray)

        assertArrayEquals(expected, actual)
    }
}

object TestKeys {
        val activeUser = SharedPrefsStoreKey(id = "computedKeyX", clazz = String::class.java)

        fun computedKey(uuid: String): SharedPrefsStoreKey<String> {
            return SharedPrefsStoreKey(id = "computedKey", uuid = uuid, clazz = String::class.java)
        }

        val memoryString = MemoryStoreKey(id = "memory_string", clazz = String::class.java)

        val encryptedString = EncryptedSharedPrefsStoreKey(
            id = "encryptedString",
            clazz = String::class.java
        )

        val encryptedComplexObject = EncryptedSharedPrefsStoreKey(
            id = "encrypted_complex_object",
            clazz = MockComplexObject::class.java
        )

        val encryptedArray = EncryptedSharedPrefsStoreKey(id = "encrypted_array", clazz = Array<String>::class.java)

        val encryptedComplexObjectArray = EncryptedSharedPrefsStoreKey(
            id = "encrypted_complex_object_array",
            clazz = Array<MockComplexObject>::class.java
        )
}

data class MockComplexObject(val name: String, val age: Int, val wallets: List<String>) {
    override fun equals(other: Any?): Boolean {
        val obj2 = other as? MockComplexObject ?: return false

        return obj2.age == age && obj2.name == name && obj2.wallets == wallets
    }
}