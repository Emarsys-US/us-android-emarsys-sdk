package com.emarsys.mobileengage.util

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.provider.timestamp.TimestampProvider
import com.emarsys.core.provider.uuid.UUIDProvider
import com.emarsys.core.request.model.RequestMethod
import com.emarsys.core.request.model.RequestModel
import com.emarsys.mobileengage.RequestContext
import com.emarsys.mobileengage.endpoint.Endpoint
import com.emarsys.testUtil.TimeoutUtils
import com.emarsys.testUtil.mockito.MockitoTestUtils.whenever
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import java.net.URL

class RequestModelUtilsTest {

    private companion object {
        const val TIMESTAMP = 123456789L
        const val REQUEST_ID = "request_id"
        const val HARDWARE_ID = "hardware_id"
        const val APPLICATION_CODE = "app_code"
        const val PUSH_TOKEN = "kjhygtfdrtrtdtguyihoj3iurf8y7t6fqyua2gyi8fhu"
    }

    lateinit var mockRequestContext: RequestContext
    lateinit var mockTimestampProvider: TimestampProvider
    lateinit var mockUuidProvider: UUIDProvider
    lateinit var mockDeviceInfo: DeviceInfo

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mockUuidProvider = mock(UUIDProvider::class.java).apply {
            whenever(provideId()).thenReturn(REQUEST_ID)
        }
        mockTimestampProvider = mock(TimestampProvider::class.java).apply {
            whenever(provideTimestamp()).thenReturn(TIMESTAMP)
        }
        mockDeviceInfo = mock(DeviceInfo::class.java).apply {
            whenever(hwid).thenReturn(HARDWARE_ID)
        }

        mockRequestContext = mock(RequestContext::class.java).apply {
            whenever(timestampProvider).thenReturn(mockTimestampProvider)
            whenever(uuidProvider).thenReturn(mockUuidProvider)
            whenever(deviceInfo).thenReturn(mockDeviceInfo)
            whenever(applicationCode).thenReturn(APPLICATION_CODE)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_requestContext_mustNotBeNull() {
        RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetPushTokenRequest_pushToken_mustNotBeNull() {
        RequestModelUtils.createSetPushTokenRequest(null, mock(RequestContext::class.java))
    }

    @Test
    fun testCreateSetPushTokenRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createSetPushTokenUrl(mockRequestContext),
                RequestMethod.PUT,
                RequestPayloadUtils.createSetPushTokenPayload(PUSH_TOKEN),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = RequestModelUtils.createSetPushTokenRequest(PUSH_TOKEN, mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateTrackDeviceInfoRequest_requestContext_mustNotBeNull() {
        RequestModelUtils.createTrackDeviceInfoRequest(null)
    }

    @Test
    fun testCreateTrackDeviceInfoRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createTrackDeviceInfoUrl(mockRequestContext),
                RequestMethod.POST,
                RequestPayloadUtils.createTrackDeviceInfoPayload(mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
        val result = RequestModelUtils.createTrackDeviceInfoRequest(mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactRequest_contactFieldValue_mustNotBeNull() {
        RequestModelUtils.createSetContactRequest(null, mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateSetContactRequest_requestContext_mustNotBeNull() {
        RequestModelUtils.createSetContactRequest("contactFieldValue", null)
    }

    @Test
    fun testCreateSetContactRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createSetContactUrl(mockRequestContext),
                RequestMethod.POST,
                RequestPayloadUtils.createSetContactPayload("contactFieldValue", mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )
        val result = RequestModelUtils.createSetContactRequest("contactFieldValue", mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCustomEvent_eventName_mustNotBeNull() {
        RequestModelUtils.createCustomEventRequest(null, emptyMap(), mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateCustomEvent_requestContext_mustNotBeNull() {
        RequestModelUtils.createCustomEventRequest("eventName", emptyMap(), null)
    }

    @Test
    fun testCreateCustomEventRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createCustomEventUrl(mockRequestContext),
                RequestMethod.POST,
                RequestPayloadUtils.createCustomEventPayload("eventName", emptyMap(), mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = RequestModelUtils.createCustomEventRequest("eventName", emptyMap(), mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInternalCustomEventRequest_eventName_mustNotBeNull() {
        RequestModelUtils.createInternalCustomEventRequest(null, emptyMap(), mockRequestContext)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCreateInternalCustomEventRequest_requestContext_mustNotBeNull() {
        RequestModelUtils.createInternalCustomEventRequest("eventName", emptyMap(), null)
    }

    @Test
    fun testCreateInternalCustomEventRequest() {
        val expected = RequestModel(
                RequestUrlUtils.createCustomEventUrl(mockRequestContext),
                RequestMethod.POST,
                RequestPayloadUtils.createInternalCustomEventPayload("eventName", emptyMap(), mockRequestContext),
                RequestHeaderUtils.createBaseHeaders_V3(mockRequestContext),
                TIMESTAMP,
                Long.MAX_VALUE,
                REQUEST_ID
        )

        val result = RequestModelUtils.createInternalCustomEventRequest("eventName", emptyMap(), mockRequestContext)

        result shouldBe expected
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsCustomEvent_V3_requestModel_mustNotBeNull() {
        RequestModelUtils.isCustomEvent_V3(null)
    }

    @Test
    fun testIsCustomEvent_V3_true_whenItIsCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_EVENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel)

        result shouldBe true
    }

    @Test
    fun testIsCustomEvent_V3_false_whenItIsNotCustomEventV3Event() {
        val requestModel = mock(RequestModel::class.java).apply {
            whenever(url).thenReturn(URL(Endpoint.ME_V3_CLIENT_BASE))
        }
        val result = RequestModelUtils.isCustomEvent_V3(requestModel)

        result shouldBe false
    }
}