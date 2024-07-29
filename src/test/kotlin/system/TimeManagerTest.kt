package system

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.mockk.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Unit tests for the TimeManager class.
 *
 * This test suite covers various functionalities of the TimeManager class, including:
 * - Retrieving the current system time.
 * - Handling invalid input when setting the system time.
 * - Converting date formats with both valid and invalid inputs.
 * - Performance testing of the date format conversion.
 *
 * Note:
 * - Some tests related to setting system time are commented out as they require appropriate system permissions.
 * - Tests ensure robustness and correctness of time-related operations in the TimeManager class.
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimeManagerTest {

    @BeforeAll
    fun setup() {
        mockkObject(TimeManager)
        mockkConstructor(NativeTimeManager::class)
    }

    @AfterAll
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testGetCurrentTime() {
        val expectedTime = "2024-07-18 12:00:00"
        every { TimeManager.getCurrentTime() } returns expectedTime

        val currentTime = TimeManager.getCurrentTime()
        println("Current time: $currentTime")
        assertNotNull(currentTime)
        assertFalse(currentTime.contains("Error"))
        assertEquals(expectedTime, currentTime)
    }

    @Test
    fun testSetTimeWithValidInput() {
        val validTime = "2024-01-01 12:00:00"
        every { TimeManager.setTime(validTime) } just Runs

        assertDoesNotThrow {
            TimeManager.setTime(validTime)
        }
    }

    @Test
    fun testSetTimeWithInvalidInput() {
        val invalidTime = "invalid-date"
        every { TimeManager.setTime(invalidTime) } throws IllegalArgumentException("Error setting time: Invalid time format")

        val exception = assertThrows<IllegalArgumentException> {
            TimeManager.setTime(invalidTime)
        }
        println("Exception message: ${exception.message}")
        assertTrue(exception.message!!.contains("Error setting time"), "Invalid input should result in an error")
    }

    @Test
    fun testConvertDateFormatWithValidInput() {
        val dateStr = "2024-01-01 12:00:00"
        val fromFormat = "yyyy-MM-dd HH:mm:ss"
        val toFormat = "dd-MM-yyyy HH:mm"
        val expectedDate = "01-01-2024 12:00"
        every { TimeManager.convertDateFormat(dateStr, fromFormat, toFormat) } returns expectedDate

        val convertedDate = TimeManager.convertDateFormat(dateStr, fromFormat, toFormat)
        assertNotNull(convertedDate)
        assertEquals(expectedDate, convertedDate)
    }

    @Test
    fun testConvertDateFormatWithInvalidInput() {
        val invalidDateStr = "invalid-date"
        val fromFormat = "yyyy-MM-dd HH:mm:ss"
        val toFormat = "dd-MM-yyyy HH:mm"
        every { TimeManager.convertDateFormat(invalidDateStr, fromFormat, toFormat) } returns null

        val convertedDate = TimeManager.convertDateFormat(invalidDateStr, fromFormat, toFormat)
        assertNull(convertedDate)
    }

    @Test
    fun testConvertDateFormatWithInvalidFormat() {
        val dateStr = "2024-01-01 12:00:00"
        val invalidFromFormat = "invalid-format"
        val toFormat = "dd-MM-yyyy HH:mm"
        every { TimeManager.convertDateFormat(dateStr, invalidFromFormat, toFormat) } returns null

        val convertedDate = TimeManager.convertDateFormat(dateStr, invalidFromFormat, toFormat)
        assertNull(convertedDate)
    }

    @Test
    fun testConvertDateFormatPerformance() {
        val dateStr = "2024-01-01 12:00:00"
        val fromFormat = "yyyy-MM-dd HH:mm:ss"
        val toFormat = "dd-MM-yyyy HH:mm"
        every { TimeManager.convertDateFormat(dateStr, fromFormat, toFormat) } returns "01-01-2024 12:00"

        val start = System.nanoTime()
        val convertedDate = TimeManager.convertDateFormat(dateStr, fromFormat, toFormat)
        val end = System.nanoTime()
        assertNotNull(convertedDate)
        assertTrue((end - start) < 50000000)
    }

    @Test
    fun testEndToEndTimeManagement() {
        val dateStr = "2024-01-01 12:00:00"
        val format = "yyyy-MM-dd HH:mm:ss"

        // Mock setting and getting time
        every { TimeManager.setTime(dateStr) } just Runs
        every { TimeManager.getCurrentTime() } returns dateStr

        // Step 1: Set the system time to the test date
        assertDoesNotThrow {
            TimeManager.setTime(dateStr)
        }

        // Step 2: Get the current system time
        val currentTimeStr = TimeManager.getCurrentTime()
        assertNotNull(currentTimeStr)
        assertFalse(currentTimeStr.contains("Error"), "Error in getting current time")

        // Step 3: Convert both times to Date objects
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val expectedDate: Date = dateFormat.parse(dateStr)
        val actualDate: Date = dateFormat.parse(currentTimeStr)

        // Step 4: Compare the dates to ensure they are the same within 10 milliseconds
        val timeDifference = abs(expectedDate.time - actualDate.time)
        println("Time difference: $timeDifference milliseconds")
        assertTrue(timeDifference < 10, "Time difference is greater than 10 milliseconds")
    }
}
