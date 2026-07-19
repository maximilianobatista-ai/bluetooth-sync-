package com.bluetoothsync.app.data

object LatencyDatabase {
    private val database = listOf(
        // Apple
        BluetoothDeviceProfile("AirPods Pro", "Apple", 150, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("AirPods Max", "Apple", 200, "AAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("AirPods 3", "Apple", 140, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("AirPods 2", "Apple", 170, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("AirPods 4", "Apple", 130, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Beats Studio", "Apple", 180, "AAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Beats Fit Pro", "Apple", 145, "AAC", DeviceCategory.EARBUDS),

        // Sony
        BluetoothDeviceProfile("WH-1000XM5", "Sony", 220, "LDAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("WH-1000XM4", "Sony", 240, "LDAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("WH-1000XM3", "Sony", 260, "LDAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("WF-1000XM5", "Sony", 180, "LDAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("WF-1000XM4", "Sony", 200, "LDAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("WH-CH720N", "Sony", 250, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("LinkBuds", "Sony", 160, "AAC", DeviceCategory.EARBUDS),

        // Samsung
        BluetoothDeviceProfile("Galaxy Buds3 Pro", "Samsung", 120, "SSC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Galaxy Buds3", "Samsung", 140, "SSC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Galaxy Buds2 Pro", "Samsung", 130, "SSC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Galaxy Buds2", "Samsung", 160, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Galaxy Buds FE", "Samsung", 170, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Galaxy Buds Live", "Samsung", 180, "AAC", DeviceCategory.EARBUDS),

        // Bose
        BluetoothDeviceProfile("QuietComfort Ultra", "Bose", 280, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("QuietComfort 45", "Bose", 260, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("QuietComfort Earbuds", "Bose", 240, "SBC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Sport Earbuds", "Bose", 230, "SBC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("SoundLink Flex", "Bose", 200, "SBC", DeviceCategory.SPEAKER),

        // JBL
        BluetoothDeviceProfile("Tune 760NC", "JBL", 200, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Tune 710BT", "JBL", 210, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Flip 6", "JBL", 180, "SBC", DeviceCategory.SPEAKER),
        BluetoothDeviceProfile("Flip 5", "JBL", 190, "SBC", DeviceCategory.SPEAKER),
        BluetoothDeviceProfile("Charge 5", "JBL", 185, "SBC", DeviceCategory.SPEAKER),
        BluetoothDeviceProfile("Tour Pro 2", "JBL", 160, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Live Pro 2", "JBL", 170, "AAC", DeviceCategory.EARBUDS),

        // Sennheiser
        BluetoothDeviceProfile("Momentum 4", "Sennheiser", 160, "aptX", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Momentum 3", "Sennheiser", 180, "aptX", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Momentum True Wireless 3", "Sennheiser", 140, "aptX", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Momentum True Wireless 4", "Sennheiser", 130, "aptX", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("CX Plus", "Sennheiser", 150, "AAC", DeviceCategory.EARBUDS),

        // Anker / Soundcore
        BluetoothDeviceProfile("Space A40", "Soundcore", 170, "LDAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Space One", "Soundcore", 190, "LDAC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Life Q30", "Soundcore", 220, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Life Q35", "Soundcore", 210, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("Liberty 4 NC", "Soundcore", 160, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Liberty 4 Pro", "Soundcore", 150, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Sport X10", "Soundcore", 180, "SBC", DeviceCategory.EARBUDS),

        // Xiaomi
        BluetoothDeviceProfile("Redmi Buds 5 Pro", "Xiaomi", 150, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Redmi Buds 5", "Xiaomi", 160, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Redmi Buds 4 Pro", "Xiaomi", 155, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Mi True Wireless 2", "Xiaomi", 200, "SBC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Mi True Wireless 3", "Xiaomi", 170, "AAC", DeviceCategory.EARBUDS),

        // Google
        BluetoothDeviceProfile("Pixel Buds Pro", "Google", 130, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Pixel Buds Pro 2", "Google", 110, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Pixel Buds A", "Google", 160, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Pixel Buds 2", "Google", 180, "AAC", DeviceCategory.EARBUDS),

        // Nothing
        BluetoothDeviceProfile("Ear (2)", "Nothing", 140, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Ear (a)", "Nothing", 150, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Ear (1)", "Nothing", 170, "AAC", DeviceCategory.EARBUDS),

        // OnePlus
        BluetoothDeviceProfile("Buds Pro 2", "OnePlus", 120, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Buds Pro", "OnePlus", 140, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("Buds Z2", "OnePlus", 160, "AAC", DeviceCategory.EARBUDS),

        // Huawei
        BluetoothDeviceProfile("FreeBuds Pro 3", "Huawei", 130, "L2HC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("FreeBuds Pro 2", "Huawei", 150, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("FreeBuds 5", "Huawei", 160, "AAC", DeviceCategory.EARBUDS),

        // Edifier
        BluetoothDeviceProfile("W820NB", "Edifier", 210, "SBC", DeviceCategory.HEADPHONES),
        BluetoothDeviceProfile("TWS1 Pro", "Edifier", 180, "AAC", DeviceCategory.EARBUDS),
        BluetoothDeviceProfile("NeoBuds Pro", "Edifier", 150, "LDAC", DeviceCategory.EARBUDS),

        // Gaming
        BluetoothDeviceProfile("Arctis 1 Wireless", "SteelSeries", 40, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("Arctis 7P", "SteelSeries", 50, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("Hammerhead Pro", "Razer", 60, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("Hammerhead X", "Razer", 55, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("Cloud Mix", "HyperX", 50, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("Cloud Flight", "HyperX", 45, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("G Pro X", "Logitech", 35, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("G735", "Logitech", 40, "aptX LL", DeviceCategory.GAMING),
        BluetoothDeviceProfile("BlackShark V2 Pro", "Razer", 48, "aptX LL", DeviceCategory.GAMING),

        // Generic / Budget
        BluetoothDeviceProfile("Generic SBC", "Generic", 250, "SBC", DeviceCategory.UNKNOWN),
        BluetoothDeviceProfile("Generic AAC", "Generic", 180, "AAC", DeviceCategory.UNKNOWN),
        BluetoothDeviceProfile("Generic aptX", "Generic", 120, "aptX", DeviceCategory.UNKNOWN),
        BluetoothDeviceProfile("Generic LDAC", "Generic", 200, "LDAC", DeviceCategory.UNKNOWN),
    )

    fun findByName(deviceName: String?): BluetoothDeviceProfile? {
        if (deviceName == null) return null
        return database.find { 
            deviceName.contains(it.name, ignoreCase = true)
        } ?: database.find {
            it.brand.equals(deviceName.split(" ").firstOrNull(), ignoreCase = true)
        }
    }

    fun getByBrand(brand: String): List<BluetoothDeviceProfile> {
        return database.filter { it.brand.equals(brand, ignoreCase = true) }
    }

    fun getAll() = database

    fun getBrands(): List<String> = database.map { it.brand }.distinct().sorted()
}
