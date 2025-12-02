package com.utils.tests;

import com.utils.models.Daily;
import com.utils.models.OpenMeteoResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenMeteoResponseTest {

    @Test
    void getDaily_ShouldReturnNullInitially() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        assertNull(response.getDaily());
    }

    @Test
    void setDaily_ShouldSetDailyObject() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        Daily daily = new Daily();

        response.setDaily(daily);

        assertNotNull(response.getDaily());
        assertEquals(daily, response.getDaily());
    }

    @Test
    void setDailyToNull_ShouldWorkCorrectly() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        Daily daily = new Daily();

        response.setDaily(daily);
        assertNotNull(response.getDaily());

        response.setDaily(null);
        assertNull(response.getDaily());
    }

    @Test
    void multipleSetDailyCalls_ShouldKeepLastValue() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        Daily daily1 = new Daily();
        Daily daily2 = new Daily();

        response.setDaily(daily1);
        assertEquals(daily1, response.getDaily());

        response.setDaily(daily2);
        assertEquals(daily2, response.getDaily());
    }

    @Test
    void getDaily_AfterSet_ShouldReturnSameInstance() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        Daily originalDaily = new Daily();

        response.setDaily(originalDaily);
        Daily retrievedDaily = response.getDaily();

        assertSame(originalDaily, retrievedDaily);
    }
}