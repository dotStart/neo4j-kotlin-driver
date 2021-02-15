package com.neo4j.driver.kotlin.delegate

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Provides a delegate implementation which lazily initializes its value upon first access.
 *
 * This implementation is thread safe and optimized for cases where a property is rarely initialized but frequently
 * accessed. Implementors may choose to expose the [reset] function in order to permit reverting the delegate to its
 * initial construction state at a later point within its lifecycle.
 */
abstract class AbstractLazyReadOnlyProperty<T, V> : ReadOnlyProperty<T, V> {

    /**
     * Permanently stores the actual value of this property.
     *
     * Note: This value has been marked [Volatile] in order to permit quick synchronization across threads thus reducing
     * the latency when rapidly accessed across threads once initialization has taken place.
     */
    @Volatile
    private var value: Any? = Uninitialized

    /**
     * Retrieves the object which shall act as a lock for this particular delegate.
     *
     * This property is expected to permanently return the same value across all calls regardless of their respective
     * context in order to prevent duplicate initialization calls.
     *
     * The default implementation of this property will return `this`. While this is typically not desirable (as other
     * objects would be able to mess with the lock), this is perfectly acceptable within the context of delegates as
     * they are only ever exposed via reflection.
     */
    protected open val lock: Any
        get() = this

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        // perform an initial poll of the current property value in order to quickly access the delegate state without
        // acquiring the lock - this is possible due to the Volatile annotation as the JVM will make sure that we see
        // the initialized state as fast as possible
        val unprotectedValue = this.value
        if (unprotectedValue !== Uninitialized) {
            @Suppress("UNCHECKED_CAST")
            return unprotectedValue as V
        }

        // assuming the property has yet to be initialized, we'll have to enter a lock in order to ensure only a single
        // initialization may take place at any given time
        synchronized(this.lock) {
            // check the value once again as it may have been initialized while this thread was waiting to acquire an
            // exclusive lock on the delegate
            val protectedValue = this.value
            if (protectedValue !== Uninitialized) {
                @Suppress("UNCHECKED_CAST")
                return protectedValue as V
            }

            // the calling thread is now guaranteed to be the first to initialize the property and may thus proceed to
            // invoke the initialization method
            val initializedValue = this.initialize(thisRef)
            this.value = initializedValue
            return initializedValue
        }
    }

    /**
     * Resets the state of this lazy property back to its uninitialized state.
     *
     * When invoked, the next access to the property via [getValue] will cause [initialize] to be invoked once again as
     * if the object had never been accessed prior to the [reset] call.
     */
    protected open fun reset() {
        synchronized(this.lock) {
            this.value = Uninitialized
        }
    }

    /**
     * Performs the lazy initialization of this property.
     *
     * Unless [reset] is exposed or directly invoked as part of the implementation, this method will be invoked exactly
     * once throughout the lifetime of a lazy property.
     *
     * This method is always invoked within a thread safe context where only a single thread may ever perform a property
     * initialization.
     */
    protected abstract fun initialize(thisRef: T): V

    /**
     * Acts a marker object which indicates that a given delegate instance has yet to be initialized.
     *
     * This value is used as a replacement for `null` as a delegate implementation may very well be nullable and take
     * this state on its own accord thus potentially causing multiple initialization calls to be performed when this is
     * not desired.
     */
    private object Uninitialized
}