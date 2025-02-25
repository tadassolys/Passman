package com.tadas.passman;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class AddItemActivityTest {

    @Rule
    public ActivityScenarioRule<AddItemActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), AddItemActivity.class));

    @Test
    public void testSaveItem() {
        // Type text into input fields
        onView(withId(R.id.editTextItemAdd)).perform(typeText("Test Item"), closeSoftKeyboard());
        onView(withId(R.id.editTextUsernameAdd)).perform(typeText("TestUser"), closeSoftKeyboard());
        onView(withId(R.id.editTextPasswordAdd)).perform(typeText("TestPass"), closeSoftKeyboard());

        // Click the save button
        onView(withId(R.id.btnSave)).perform(click());
    }
}
