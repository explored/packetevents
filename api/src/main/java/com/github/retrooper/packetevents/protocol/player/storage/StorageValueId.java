package com.github.retrooper.packetevents.protocol.player.storage;

public final class StorageValueId extends StorageIdBase {

    StorageValueId(int id) {
        super(id);
    }

    /**
     * Creates a constant Storage Identifier.
     * The explanation of the intended usage of which can be found in the class
     * description of {@link FastUserStorage}.
     *
     * @apiNote
     * Ideally, this method should only be called once, and the returned Identifier should be reused.
     * You CAN, however, call this method many times (for the ease of use), as each plugin's
     * constant Identifier is internally cached. Just be wary to always invoke this method with
     * your actual plugin instance.
     *
     * @param plugin The plugin the constant identifier should be created for
     *
     */
    public static StorageValueId identifierFor(Object plugin) {
        return FastUserStorage.identifierFor(plugin);
    }
}