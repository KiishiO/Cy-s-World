package com.example.own_example;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.own_example.adapters.AdminClassesAdapter;
import com.example.own_example.adapters.SelectedStudentsAdapter;
import com.example.own_example.models.Student;
import com.example.own_example.models.Teacher;
import com.example.own_example.models.ClassModel;
import com.example.own_example.services.AdminClassesService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for administrators to manage classes
 */
public class AdminClassesActivity extends AppCompatActivity implements
        AdminClassesAdapter.OnClassActionListener,
        SelectedStudentsAdapter.OnStudentRemovedListener {

    private static final String TAG = "AdminClassesActivity";

    // UI components for main screen
    private RecyclerView classesRecyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private ProgressBar loadingIndicator;
    private TextView classesCount;
    private MaterialButton createClassButton;

    // Data and adapters
    private AdminClassesAdapter classesAdapter;
    private List<ClassModel> classesList = new ArrayList<>();
    private List<Teacher> teachersList = new ArrayList<>();
    private List<Student> allStudentsList = new ArrayList<>();

    // Service
    private AdminClassesService classesService;

    // Dialog components and data
    private AlertDialog editDialog;
    private TextInputEditText classNameInput;
    private TextInputEditText locationInput;
    private MaterialAutoCompleteTextView teacherSpinner;
    private MaterialAutoCompleteTextView studentSpinner;
    private ChipGroup dayChipGroup;
    private TextInputEditText startTimeInput;
    private TextInputEditText endTimeInput;
    private RecyclerView selectedStudentsRecyclerView;
    private TextView selectedStudentsLabel;
    private SelectedStudentsAdapter selectedStudentsAdapter;
    private List<Student> selectedStudents = new ArrayList<>();
    private Teacher selectedTeacher; // Store the selected teacher
    private Student selectedStudent; // Store the selected student

    // Time formatting
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    // Current class being edited
    private ClassModel currentEditClass;
    private int currentEditPosition = -1;

    // For tracking operations
    private final int[] pendingOperations = {0};
    private final boolean[] hasErrors = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_classes);

        try {
            // Initialize service
            classesService = new AdminClassesService(this);

            // Initialize views
            classesRecyclerView = findViewById(R.id.admin_classes_recycler_view);
            swipeRefresh = findViewById(R.id.swipe_refresh);
            emptyState = findViewById(R.id.empty_state);
            loadingIndicator = findViewById(R.id.loading_indicator);
            classesCount = findViewById(R.id.classes_count);
            createClassButton = findViewById(R.id.create_class_button);

            // Setup adapter
            classesAdapter = new AdminClassesAdapter(this, classesList, this);
            classesRecyclerView.setAdapter(classesAdapter);

            // Set up swipe to refresh
            swipeRefresh.setOnRefreshListener(this::loadClasses);

            // Set up create button
            createClassButton.setOnClickListener(v -> showClassDialog(null, -1));

            // Load initial data
            loadClasses();
            loadTeachers();
            loadStudents();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showSnackbar("Error initializing the Admin Classes screen");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadClasses();
    }

    /**
     * Load all classes
     */
    private void loadClasses() {
        showLoading(true);

        classesService.getAllClasses(new AdminClassesService.ListCallback<ClassModel>() {
            @Override
            public void onSuccess(List<ClassModel> result) {
                classesList.clear();
                classesList.addAll(result);
                updateUI();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showSnackbar("Error loading classes: " + errorMessage);
            }
        });
    }

    /**
     * Load all teachers for selection in class dialog
     */
    private void loadTeachers() {
        classesService.getAllTeachers(new AdminClassesService.ListCallback<Teacher>() {
            @Override
            public void onSuccess(List<Teacher> result) {
                teachersList.clear();
                teachersList.addAll(result);
            }

            @Override
            public void onError(String errorMessage) {
                showSnackbar("Error loading teachers: " + errorMessage);
            }
        });
    }

    /**
     * Load all students for assignment in class dialog
     */
    private void loadStudents() {
        classesService.getAllStudents(new AdminClassesService.ListCallback<Student>() {
            @Override
            public void onSuccess(List<Student> result) {
                allStudentsList.clear();
                allStudentsList.addAll(result);
            }

            @Override
            public void onError(String errorMessage) {
                showSnackbar("Error loading students: " + errorMessage);
            }
        });
    }

    /**
     * Update the UI with loaded data
     */
    private void updateUI() {
        runOnUiThread(() -> {
            showLoading(false);

            // Update classes count
            classesCount.setText(String.valueOf(classesList.size()));

            // Show/hide empty state
            if (classesList.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                classesRecyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                classesRecyclerView.setVisibility(View.VISIBLE);
            }

            // Update adapter
            classesAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Show or hide loading indicators
     */
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                loadingIndicator.setVisibility(View.VISIBLE);
                swipeRefresh.setRefreshing(false);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    /**
     * Show the class edit dialog for creating or editing a class
     */
    private void showClassDialog(ClassModel classModel, int position) {
        // Store reference to the class being edited
        currentEditClass = classModel;
        currentEditPosition = position;

        // Determine if we're creating a new class or editing an existing one
        boolean isNewClass = (classModel == null);

        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_class_edit, null);
        builder.setView(dialogView);

        // Initialize dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        classNameInput = dialogView.findViewById(R.id.class_name_input);
        locationInput = dialogView.findViewById(R.id.location_input);
        TextInputLayout teacherSpinnerLayout = dialogView.findViewById(R.id.teacher_spinner_layout);
        teacherSpinner = dialogView.findViewById(R.id.teacher_spinner);
        TextInputLayout studentSpinnerLayout = dialogView.findViewById(R.id.student_spinner_layout);
        studentSpinner = dialogView.findViewById(R.id.student_spinner);
        dayChipGroup = dialogView.findViewById(R.id.day_chip_group);
        startTimeInput = dialogView.findViewById(R.id.start_time_input);
        endTimeInput = dialogView.findViewById(R.id.end_time_input);
        selectedStudentsRecyclerView = dialogView.findViewById(R.id.selected_students_recycler_view);
        selectedStudentsLabel = dialogView.findViewById(R.id.selected_students_label);
        MaterialButton addStudentButton = dialogView.findViewById(R.id.add_student_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Set dialog title
        dialogTitle.setText(isNewClass ? "Create New Class" : "Edit Class");

        // Setup teacher spinner
        setupTeacherSpinner();

        // Setup student spinner
        setupStudentSpinner();

        // Setup students recycler view
        selectedStudents.clear();
        selectedStudentsAdapter = new SelectedStudentsAdapter(this, selectedStudents, this);
        selectedStudentsRecyclerView.setAdapter(selectedStudentsAdapter);

        // Setup time pickers
        setupTimePickers();

        // If editing, populate fields with existing data
        if (!isNewClass) {
            populateClassData(classModel);
        }

        // Setup add student button
        addStudentButton.setOnClickListener(v -> {
            if (selectedStudent != null) {
                // Add to selected students
                selectedStudentsAdapter.addStudent(selectedStudent);
                studentSpinner.setText(""); // Clear selection
                selectedStudent = null;
                updateSelectedStudentsCount();
            } else {
                showSnackbar("Please select a student first");
            }
        });

        // Setup save button
        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveClass();
            }
        });

        // Setup cancel button
        cancelButton.setOnClickListener(v -> editDialog.dismiss());

        // Show the dialog
        editDialog = builder.create();
        editDialog.show();
    }

    /**
     * Setup the teacher spinner with teacher data
     */
    private void setupTeacherSpinner() {
        // Create an array of teacher names for display
        String[] teacherNames = new String[teachersList.size()];
        for (int i = 0; i < teachersList.size(); i++) {
            teacherNames[i] = teachersList.get(i).getName();
        }

        // Set up adapter for MaterialAutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, teacherNames);
        teacherSpinner.setAdapter(adapter);

        // Handle teacher selection
        teacherSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTeacherName = teacherNames[position];
            // Find the matching teacher in the list
            for (Teacher teacher : teachersList) {
                if (teacher.getName().equals(selectedTeacherName)) {
                    selectedTeacher = teacher;
                    break;
                }
            }
        });
    }

    /**
     * Setup the student spinner with student data
     */
    private void setupStudentSpinner() {
        // Create an array of student names for display
        String[] studentNames = new String[allStudentsList.size()];
        for (int i = 0; i < allStudentsList.size(); i++) {
            studentNames[i] = allStudentsList.get(i).getName();
        }

        // Set up adapter for MaterialAutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, studentNames);
        studentSpinner.setAdapter(adapter);

        // Handle student selection
        studentSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedStudentName = studentNames[position];
            // Find matching student
            for (Student student : allStudentsList) {
                if (student.getName().equals(selectedStudentName)) {
                    selectedStudent = student;
                    break;
                }
            }
        });
    }

    /**
     * Setup time picker dialogs for start and end time fields
     */
    private void setupTimePickers() {
        startTimeInput.setOnClickListener(v -> showTimePickerDialog(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePickerDialog(endTimeInput));
    }

    /**
     * Show a time picker dialog for the given input field
     */
    private void showTimePickerDialog(TextInputEditText timeInput) {
        int hour = 8, minute = 0; // Default time

        // Parse current time if it exists
        String currentTime = timeInput.getText().toString();
        if (!currentTime.isEmpty()) {
            try {
                LocalTime time = LocalTime.parse(currentTime, timeFormatter);
                hour = time.getHour();
                minute = time.getMinute();
            } catch (Exception e) {
                Log.e(TAG, "Error parsing time: " + e.getMessage());
            }
        }

        // Create and show time picker
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (TimePickerDialog.OnTimeSetListener) (TimePicker view, int hourOfDay, int selectedMinute) -> {
                    LocalTime time = LocalTime.of(hourOfDay, selectedMinute);
                    timeInput.setText(time.format(timeFormatter));
                },
                hour, minute, false);

        timePicker.show();
    }

    /**
     * Populate the dialog with existing class data
     */
    private void populateClassData(ClassModel classModel) {
        classNameInput.setText(classModel.getClassName());
        locationInput.setText(classModel.getLocation());

        // Set teacher selection
        if (classModel.getTeacherId() > 0) {
            for (Teacher teacher : teachersList) {
                if (teacher.getId() == classModel.getTeacherId()) {
                    teacherSpinner.setText(teacher.getName());
                    selectedTeacher = teacher;
                    break;
                }
            }
        }

        // Set schedule data
        if (classModel.getSchedules() != null && !classModel.getSchedules().isEmpty()) {
            // We'll use the first schedule item for time inputs
            ClassModel.ScheduleItem firstSchedule = classModel.getSchedules().get(0);
            startTimeInput.setText(firstSchedule.getStartTime().format(timeFormatter));
            endTimeInput.setText(firstSchedule.getEndTime().format(timeFormatter));

            // Set day chips based on all schedules
            Map<DayOfWeek, Chip> dayMap = new HashMap<>();
            dayMap.put(DayOfWeek.MONDAY, dayChipGroup.findViewById(R.id.monday_chip));
            dayMap.put(DayOfWeek.TUESDAY, dayChipGroup.findViewById(R.id.tuesday_chip));
            dayMap.put(DayOfWeek.WEDNESDAY, dayChipGroup.findViewById(R.id.wednesday_chip));
            dayMap.put(DayOfWeek.THURSDAY, dayChipGroup.findViewById(R.id.thursday_chip));
            dayMap.put(DayOfWeek.FRIDAY, dayChipGroup.findViewById(R.id.friday_chip));

            for (ClassModel.ScheduleItem schedule : classModel.getSchedules()) {
                Chip dayChip = dayMap.get(schedule.getDayOfWeek());
                if (dayChip != null) {
                    dayChip.setChecked(true);
                }
            }
        }

        // Add selected students based on class student IDs
        if (classModel.getStudentIds() != null && !classModel.getStudentIds().isEmpty()) {
            for (Integer studentId : classModel.getStudentIds()) {
                for (Student student : allStudentsList) {
                    if (student.getId() == studentId) {
                        selectedStudents.add(student);
                        break;
                    }
                }
            }

            // Update the selected students counter
            updateSelectedStudentsCount();
        }
    }

    /**
     * Update the counter for selected students
     */
    private void updateSelectedStudentsCount() {
        int count = selectedStudentsAdapter.getItemCount();
        selectedStudentsLabel.setText("Selected Students (" + count + ")");
    }

    /**
     * Validate the form before saving
     */
    private boolean validateForm() {
        String className = classNameInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();
        String teacherName = teacherSpinner.getText().toString().trim();

        // Check for required fields
        if (className.isEmpty()) {
            showSnackbar("Please enter a class name");
            return false;
        }

        if (location.isEmpty()) {
            showSnackbar("Please enter a location");
            return false;
        }

        if (teacherName.isEmpty() || selectedTeacher == null) {
            showSnackbar("Please select a teacher");
            return false;
        }

        if (startTime.isEmpty() || endTime.isEmpty()) {
            showSnackbar("Please set start and end times");
            return false;
        }

        // Check if at least one day is selected
        boolean anyDaySelected = false;
        for (int i = 0; i < dayChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) dayChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                anyDaySelected = true;
                break;
            }
        }

        if (!anyDaySelected) {
            showSnackbar("Please select at least one day for the class");
            return false;
        }

        // Check if there are any selected students
        if (selectedStudentsAdapter.getItemCount() == 0) {
            showSnackbar("Please add at least one student to the class");
            return false;
        }

        return true;
    }

    /**
     * Save the class data
     */
    private void saveClass() {
        // Get form data
        String className = classNameInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();

        // Parse times
        LocalTime startTime = LocalTime.parse(startTimeInput.getText().toString(), timeFormatter);
        LocalTime endTime = LocalTime.parse(endTimeInput.getText().toString(), timeFormatter);

        // Create or update class model
        ClassModel classModel;
        if (currentEditClass != null) {
            // Update existing class
            classModel = currentEditClass;
            classModel.setClassName(className);
            classModel.setLocation(location);
            classModel.setTeacherId(selectedTeacher.getId());
            classModel.setTeacherName(selectedTeacher.getName());

            // Clear existing schedules
            classModel.setSchedules(new ArrayList<>());
        } else {
            // Create new class
            classModel = new ClassModel(0, className, selectedTeacher.getId(), selectedTeacher.getName(), location);
            classModel.setSchedules(new ArrayList<>()); // Initialize schedules list
        }

        // Null check before proceeding
        if (classModel == null) {
            showSnackbar("Error: Failed to create class model");
            return;
        }

        // Add schedules based on selected days
        Map<Integer, DayOfWeek> dayMap = new HashMap<>();
        dayMap.put(R.id.monday_chip, DayOfWeek.MONDAY);
        dayMap.put(R.id.tuesday_chip, DayOfWeek.TUESDAY);
        dayMap.put(R.id.wednesday_chip, DayOfWeek.WEDNESDAY);
        dayMap.put(R.id.thursday_chip, DayOfWeek.THURSDAY);
        dayMap.put(R.id.friday_chip, DayOfWeek.FRIDAY);

        for (int i = 0; i < dayChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) dayChipGroup.getChildAt(i);
            if (chip.isChecked() && dayMap.containsKey(chip.getId())) {
                DayOfWeek day = dayMap.get(chip.getId());
                classModel.addSchedule(new ClassModel.ScheduleItem(day, startTime, endTime));
            }
        }

        // Show loading
        showLoading(true);

        if (currentEditClass != null) {
            // Update existing class
            final ClassModel finalClassModel = classModel;
            classesService.updateClass(classModel, new AdminClassesService.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    // Now add/update students
                    updateClassStudents(finalClassModel);
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showSnackbar("Error updating class: " + errorMessage);
                }
            });
        } else {
            // Create new class
            final ClassModel finalClassModel = classModel;
            classesService.createClass(classModel, new AdminClassesService.ActionCallback() {
                @Override
                public void onSuccess(String message) {
                    // Check if the class model has an ID now
                    if (finalClassModel.getId() > 0) {
                        // Add students to the newly created class
                        updateClassStudents(finalClassModel);
                    } else {
                        // If we don't get the ID from the response, fetch the classes to find the new class
                        classesService.getAllClasses(new AdminClassesService.ListCallback<ClassModel>() {
                            @Override
                            public void onSuccess(List<ClassModel> result) {
                                // Find the newly created class by name and location
                                for (ClassModel newClass : result) {
                                    if (newClass.getClassName().equals(finalClassModel.getClassName()) &&
                                            newClass.getLocation().equals(finalClassModel.getLocation())) {
                                        // Update the class model with the new ID
                                        finalClassModel.setId(newClass.getId());
                                        // Now add students to the class
                                        updateClassStudents(finalClassModel);
                                        return;
                                    }
                                }
                                // If we can't find the class, show a message and reload
                                showLoading(false);
                                editDialog.dismiss();
                                // In saveClass() after creating a new class
                                Log.d(TAG, "Created new class, ID: " + finalClassModel.getId());
                                showSnackbar("Class created successfully but couldn't add students");
                                loadClasses();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                showLoading(false);
                                showSnackbar("Class created but error retrieving class ID: " + errorMessage);
                                loadClasses();
                            }
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showSnackbar("Error creating class: " + errorMessage);
                }
            });
        }
    }

    /**
     * Update students assigned to a class
     */
    private void updateClassStudents(ClassModel classModel) {
        // First verify we have a valid class ID
        if (classModel.getId() <= 0) {
            Log.e(TAG, "Cannot update students: Invalid class ID");
            finishOperation(true);
            return;
        }

        // Get selected students from adapter
        List<Student> students = selectedStudentsAdapter.getStudents();

        // Log for debugging
        Log.d(TAG, "Updating class " + classModel.getId() + " with " + students.size() + " students");
        Log.d(TAG, "Current student IDs in class: " + classModel.getStudentIds());
        Log.d(TAG, "Students to assign: " + students);

        // Reset operation counters
        pendingOperations[0] = 0;
        hasErrors[0] = false;

        // Create a list to keep track of new student IDs
        List<Integer> updatedStudentIds = new ArrayList<>();
        for (Student student : students) {
            updatedStudentIds.add(student.getId());
        }

        // Add new students
        for (Student student : students) {
            if (!classModel.getStudentIds().contains(student.getId())) {
                pendingOperations[0]++;
                classesService.addStudentToClass(classModel.getId(), student.getId(), new AdminClassesService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Added student " + student.getId() + " to class " + classModel.getId());
                        checkCompletion();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error adding student: " + errorMessage);
                        hasErrors[0] = true;
                        // Remove the student ID from the updated list since it failed
                        updatedStudentIds.remove(Integer.valueOf(student.getId()));
                        checkCompletion();
                    }
                });
            }
        }

        // Remove students no longer in selection
        for (Integer studentId : classModel.getStudentIds()) {
            boolean found = false;
            for (Student student : students) {
                if (student.getId() == studentId) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                pendingOperations[0]++;
                classesService.removeStudentFromClass(classModel.getId(), studentId, new AdminClassesService.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Removed student " + studentId + " from class " + classModel.getId());
                        checkCompletion();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error removing student: " + errorMessage);
                        hasErrors[0] = true;
                        // Add the student ID back to the updated list since removal failed
                        updatedStudentIds.add(studentId);
                        checkCompletion();
                    }
                });
            }
        }

        // Update the class model with the new student IDs
        classModel.setStudentIds(new ArrayList<>(updatedStudentIds));

        // If no operations are pending, complete now
        if (pendingOperations[0] == 0) {
            finishOperation(false);
        }
    }

    /**
     * Helper method to check if all operations are done
     */
    private void checkCompletion() {
        pendingOperations[0]--;
        Log.d(TAG, "Operations remaining: " + pendingOperations[0]);
        Log.d(TAG, "Pending operations: " + pendingOperations[0] + ", hasErrors: " + hasErrors[0]);
        if (pendingOperations[0] <= 0) {
            finishOperation(hasErrors[0]);
        }
    }

    /**
     * Helper method to finish and update UI
     */
    private void finishOperation(boolean hasErrors) {
        // Get the position of the current class if we're editing
        int position = -1;
        if (currentEditClass != null && currentEditPosition >= 0) {
            position = currentEditPosition;
        } else if (currentEditClass != null) {
            // Find the class in the list if position wasn't provided
            for (int i = 0; i < classesList.size(); i++) {
                if (classesList.get(i).getId() == currentEditClass.getId()) {
                    position = i;
                    break;
                }
            }
        }

        // If we found the class position, update it in the list
        if (position >= 0 && position < classesList.size()) {
            classesList.set(position, currentEditClass);
            classesAdapter.notifyItemChanged(position);
        }

        // Reload classes to get updated data from server
        loadClasses();

        showLoading(false);
        if (editDialog != null && editDialog.isShowing()) {
            editDialog.dismiss();
        }

        if (hasErrors) {
            showSnackbar("Class updated but some student updates failed");
        } else {
            showSnackbar("Class updated successfully");
        }
    }

    /**
     * Handle edit class action
     */
    @Override
    public void onEditClass(ClassModel classModel, int position) {
        showClassDialog(classModel, position);
    }

    /**
     * Handle delete class action
     */
    @Override
    public void onDeleteClass(int classId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading(true);
                    classesService.deleteClass(classId, new AdminClassesService.ActionCallback() {
                        @Override
                        public void onSuccess(String message) {
                            runOnUiThread(() -> {
                                classesAdapter.removeItem(position);
                                updateUI();
                                showSnackbar("Class deleted successfully");
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            showLoading(false);
                            showSnackbar("Error deleting class: " + errorMessage);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Handle student removal from selected students
     */
    @Override
    public void onStudentRemoved(Student student, int position) {
        selectedStudentsAdapter.removeStudent(position);
        updateSelectedStudentsCount();
    }

    /**
     * Show a snackbar message
     */
    private void showSnackbar(String message) {
        runOnUiThread(() -> {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        });
    }
}