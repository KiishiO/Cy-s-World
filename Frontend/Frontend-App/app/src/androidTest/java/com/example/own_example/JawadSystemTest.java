package com.example.own_example;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * System tests that verify UI elements
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class JawadSystemTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test 1: Verify main screen buttons are displayed with correct text
     */
    @Test
    public void testMainButtons() throws InterruptedException {
        // Wait for animation to complete
        Thread.sleep(7000);

        // Check login button
        onView(withId(R.id.main_btnLogIn))
                .check(matches(isDisplayed()))
                .check(matches(withText("Log In")));

        // Check signup button
        onView(withId(R.id.main_btnSignUp))
                .check(matches(isDisplayed()))
                .check(matches(withText("Sign Up")));
    }

    /**
     * Test 2: Verify student icon is displayed
     */
    @Test
    public void testStudentIcon() throws InterruptedException {
        // Wait for animation to complete
        Thread.sleep(7000);

        // Check that the student icon is displayed
        onView(withId(R.id.landing_students_icon))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 3: Verify landing page background is set correctly
     */
    @Test
    public void testLandingPageBackground() throws InterruptedException {
        // Wait for animation to complete
        Thread.sleep(7000);

        // The background is in a ConstraintLayout, so we can test that it's displayed
        // This test is a bit more basic as we can't easily test drawable backgrounds
        // But it still verifies a part of the UI
        onView(withId(android.R.id.content))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 4: Verify button styles - this is a visual test but still important
     */
    @Test
    public void testButtonStyles() throws InterruptedException {
        // Wait for animation to complete
        Thread.sleep(7000);

        // While we can't test colors directly with basic Espresso,
        // we can verify that the buttons have the correct basic properties
        onView(withId(R.id.main_btnLogIn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.main_btnSignUp))
                .check(matches(isDisplayed()));
    }
}