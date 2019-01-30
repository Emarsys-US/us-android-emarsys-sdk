package com.emarsys.core.util.log

import com.emarsys.core.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.core.shard.ShardModel
import com.emarsys.testUtil.RandomTestUtils
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class LogShardListMergerTest {

    companion object {
        const val ID = "id"
        const val TIMESTAMP = 125L
        const val TTL = Long.MAX_VALUE
    }

    lateinit var merger: LogShardListMerger

    lateinit var timestampProvider: TimestampProvider
    lateinit var uuidProvider: UUIDProvider
    lateinit var deviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        timestampProvider = Mockito.mock(TimestampProvider::class.java)
        whenever(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP)

        uuidProvider = Mockito.mock(UUIDProvider::class.java)
        whenever(uuidProvider.provideId()).thenReturn(ID)

        deviceInfo = Mockito.mock(DeviceInfo::class.java)
        whenever(deviceInfo.platform).thenReturn("android")
        whenever(deviceInfo.applicationVersion).thenReturn("1.0.0")
        whenever(deviceInfo.osVersion).thenReturn("8.0")
        whenever(deviceInfo.model).thenReturn("Pixel")
        whenever(deviceInfo.hwid).thenReturn("hardwareId")
        whenever(deviceInfo.sdkVersion).thenReturn("1.6.1")

        merger = LogShardListMerger(timestampProvider, uuidProvider, deviceInfo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotBeNull() {
        merger.map(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustNotContainNullElements() {
        merger.map(listOf(
                Mockito.mock(ShardModel::class.java),
                null,
                Mockito.mock(ShardModel::class.java)
        ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMap_shards_mustContainAtLeastOneElement() {
        merger.map(listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_deviceInfo_mustNotBeNull() {
        LogShardListMerger(timestampProvider, uuidProvider, null)
    }

    @Test
    fun testMap_singletonList() {
        val shardData = RandomTestUtils.randomMap()
        val type = "log_crash"
        val shard = ShardModel("id", type, shardData, 1234, 4321)

        val logData = shardData + mapOf("type" to type) + mapOf("device_info" to createDeviceInfo())
        val requestPayload = mapOf("logs" to listOf(logData))
        val expectedRequestModel = requestModel(requestPayload)

        assertEquals(expectedRequestModel, merger.map(listOf(shard)))
    }

    @Test
    fun testMap_multipleElementsInList() {
        val shards = (1..5).map { randomShardModel() }
        val deviceInfo = createDeviceInfo()
        val logDatas = shards.map { it.data + mapOf("type" to it.type) + mapOf("device_info" to deviceInfo) }

        val expectedRequestModel = requestModel(mapOf("logs" to logDatas))

        merger.map(shards) shouldBe expectedRequestModel
    }

    private fun randomShardModel() = ShardModel(
            RandomTestUtils.randomString(),
            "log_${RandomTestUtils.randomString()}",
            RandomTestUtils.randomMap(),
            RandomTestUtils.randomInt().toLong(),
            RandomTestUtils.randomInt().toLong())

    private fun requestModel(payload: Map<String, Any>) = RequestModel(
            "https://ems-log-dealer.herokuapp.com/v1/log",
            RequestMethod.POST,
            payload,
            mapOf(),
            TIMESTAMP,
            TTL,
            ID
    )

    private fun createDeviceInfo(): Map<String, String> {
        return mapOf(
                "platform" to "android",
                "app_version" to "1.0.0",
                "sdk_version" to "1.6.1",
                "os_version" to "8.0",
                "model" to "Pixel",
                "hw_id" to "hardwareId"
        )
    }

}