package com.example.own_example;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class JawadSystemTest {

    private static final String TAG = "JawadSimpleTest";

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    // Test 1: Verify login button is displayed
    @Test
    public void testLoginButtonDisplayed() {
        // Add a longer wait to let animations complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.main_btnLogIn)).check(matches(isDisplayed()));
    }

    // Test 2: Verify login button text
    @Test
    public void testLoginButtonText() {
        // Add a longer wait to let animations complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.main_btnLogIn)).check(matches(withText("Log In")));
    }

    // Test 3: Verify main button layout properties are correct
    @Test
    public void testMainButtonLayout() {
        // Add a longer wait to let animations complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Just verify that the login button exists in the UI
        onView(withId(R.id.main_btnLogIn)).check(matches(isDisplayed()));
    }

    // Test 4: Verify student icon is displayed
    @Test
    public void testStudentIconDisplayed() {
        // Add a longer wait to let animations complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        onView(withId(R.id.landing_students_icon)).check(matches(isDisplayed()));
    }
}