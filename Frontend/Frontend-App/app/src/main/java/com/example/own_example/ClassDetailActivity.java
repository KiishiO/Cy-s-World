package com.example.own_example;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.adapters.AssignmentsAdapter;
import com.example.own_example.adapters.StudentsAdapter;
import com.example.own_example.models.AssignmentModel;
import com.example.own_example.models.ClassModel;
import com.example.own_example.models.Student;
import com.example.own_example.services.ClassesService;
import com.example.own_example.services.GradesService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying class details
 */
public class ClassDetailActivity extends AppCompatActivity implements
        StudentsAdapter.OnGradeUpdatedListener,
        AssignmentsAdapter.OnAssignmentClickListener {

    private static final String TAG = "ClassDetailActivity";

    // UI components for class info
    private TextView classDetailHeader;
    private TextView teacherName;
    private TextView scheduleDetails;
    private TextView locationDetails;
    private RecyclerView studentsRecyclerView;
    private MaterialCardView studentsCard;
    private ProgressBar loadingIndicator;

    // UI components for assignments
    private MaterialCardView assignmentsCard;
    private TextView assignmentsTitle;
    private TextView classAverageText;
    private TextView studentOverallGradeText;
    private RecyclerView assignmentsRecyclerView;
    private TextView emptyAssignmentsText;
    private FloatingActionButton addAssignmentButton;

    // Services
    private ClassesService classesService;
    private GradesService gradesService;

    // Adapters
    private StudentsAdapter studentsAdapter;
    private AssignmentsAdapter assignmentsAdapter;

    // Data
    private int classId;
    private int userId;
    private ClassModel currentClassModel;
    private String userRole;
    private List<Student> studentsList = new ArrayList<>();
    private List<AssignmentModel> assignmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        try {
            // Get class ID from intent
            classId = getIntent().getIntExtra("CLASS_ID", -1);
            if (classId == -1) {
                showError("Invalid class selected");
                finish();
                return;
            }

            // Get user information
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userRole = prefs.getString("user_role", "student");
            userId = prefs.getInt("user_id", -1);

            // Initialize API services
            classesService = new ClassesService(this);
            gradesService = new GradesService(this);

            // Initialize views for class info
            initClassInfoViews();

            // Initialize views for assignments
            initAssignmentsViews();

            // Setup students adapter
            studentsAdapter = new StudentsAdapter(this, new ArrayList<>(), classId, this);
            studentsRecyclerView.setAdapter(studentsAdapter);

            // Setup assignments adapter
            assignmentsAdapter = new AssignmentsAdapter(this, assignmentsList,
                    userRole.equalsIgnoreCase("teacher"), this);
            assignmentsRecyclerView.setAdapter(assignmentsAdapter);

            // Show appropriate UI based on user role
            setupUIBasedOnRole();

            // Load class details
            loadClassDetails();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showError("Error initializing the Class Details screen");
        }
    }

    /**
     * Initialize views for class information
     */
    private void initClassInfoViews() {
        classDetailHeader = findViewById(R.id.class_detail_header);
        teacherName = findViewById(R.id.teacher_name);
        scheduleDetails = findViewById(R.id.schedule_details);
        locationDetails = findViewById(R.id.location_details);
        studentsRecyclerView = findViewById(R.id.students_recycler_view);
        studentsCard = findViewById(R.id.students_card);
        loadingIndicator = findViewById(R.id.loading_indicator);
    }

    /**
     * Initialize views for assignments
     */
    private void initAssignmentsViews() {
        // This layout should be included in your activity_class_detail.xml
        assignmentsCard = findViewById(R.id.assignments_card);
        assignmentsTitle = findViewById(R.id.assignments_title);
        classAverageText = findViewById(R.id.class_average_text);
        studentOverallGradeText = findViewById(R.id.student_overall_grade_text);
        assignmentsRecyclerView = findViewById(R.id.assignments_recycler_view);
        emptyAssignmentsText = findViewById(R.id.empty_assignments_text);
        addAssignmentButton = findViewById(R.id.add_assignment_button);

        // Set click listener for add assignment button
        addAssignmentButton.setOnClickListener(v -> showAssignmentDialog(null, -1));
    }

    /**
     * Setup UI based on user role
     */
    private void setupUIBasedOnRole() {
        if (userRole.equalsIgnoreCase("teacher")) {
            // Teachers can see student list and class average
            studentsCard.setVisibility(View.VISIBLE);
            classAverageText.setVisibility(View.VISIBLE);
            studentOverallGradeText.setVisibility(View.GONE);
            addAssignmentButton.setVisibility(View.VISIBLE);
            assignmentsTitle.setText("Class Assignments");
        } else {
            // Students see their grades and cannot add assignments
            studentsCard.setVisibility(View.GONE);
            classAverageText.setVisibility(View.GONE);
            studentOverallGradeText.setVisibility(View.VISIBLE);
            addAssignmentButton.setVisibility(View.GONE);
            assignmentsTitle.setText("My Assignments");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadClassDetails();
    }

    /**
     * Load class details from API
     */
    private void loadClassDetails() {
        showLoading(true);

        classesService.getClassDetails(classId, new ClassesService.ApiCallback<ClassModel>() {
            @Override
            public void onSuccess(ClassModel result) {
                currentClassModel = result;
                updateUI(result);

                // Load students list if user is teacher
                if (userRole.equalsIgnoreCase("teacher")) {
                    loadStudents();
                }

                // Load assignments based on role
                loadAssignments();
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showError(errorMessage);
            }
        });
    }

    /**
     * Load students enrolled in the class
     */
    private void loadStudents() {
        classesService.getStudentsInClass(classId, new ClassesService.ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> result) {
                studentsList.clear();
                studentsList.addAll(result);
                updateStudentsUI(result);
            }

            @Override
            public void onError(String errorMessage) {
                showError("Error loading students: " + errorMessage);
            }
        });
    }

    /**
     * Load assignments based on user role
     */
    private void loadAssignments() {
        showLoading(true);

        if (userRole.equalsIgnoreCase("teacher")) {
            // Teacher: Load all assignments for this class
            gradesService.getClassAssignments(classId, new GradesService.ApiCallback<List<AssignmentModel>>() {
                @Override
                public void onSuccess(List<AssignmentModel> result) {
                    assignmentsList.clear();
                    assignmentsList.addAll(result);
                    updateAssignmentsUI();

                    // Also load class average
                    loadClassAverage();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showError("Error loading assignments: " + errorMessage);
                }
            });
        } else {
            // Student: Load assignments for this student in this class
            gradesService.getStudentClassAssignments(classId, userId, new GradesService.ApiCallback<List<AssignmentModel>>() {
                @Override
                public void onSuccess(List<AssignmentModel> result) {
                    assignmentsList.clear();
                    assignmentsList.addAll(result);
                    updateAssignmentsUI();

                    // Also load student's overall grade
                    loadStudentOverallGrade();
                }

                @Override
                public void onError(String errorMessage) {
                    showLoading(false);
                    showError("Error loading assignments: " + errorMessage);
                }
            });
        }
    }

    /**
     * Load class average grade
     */
    private void loadClassAverage() {
        gradesService.getClassAverageGrade(classId, new GradesService.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                updateClassAverageUI(result);
            }

            @Override
            public void onError(String errorMessage) {
                // Just log the error and don't show to user since this is non-critical
                Log.e(TAG, "Error loading class average: " + errorMessage);
            }
        });
    }

    /**
     * Load student's overall grade
     */
    private void loadStudentOverallGrade() {
        gradesService.getStudentOverallGrade(classId, userId, new GradesService.ApiCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                updateStudentOverallGradeUI(result);
            }

            @Override
            public void onError(String errorMessage) {
                // Just log the error and don't show to user since this is non-critical
                Log.e(TAG, "Error loading student's overall grade: " + errorMessage);
            }
        });
    }

    /**
     * Update the UI with class details
     */
    private void updateUI(ClassModel classModel) {
        runOnUiThread(() -> {
            showLoading(false);

            // Update header
            classDetailHeader.setText(classModel.getClassName());

            // Update teacher info
            teacherName.setText(classModel.getTeacherName());

            // Update schedule
            scheduleDetails.setText(classModel.getFormattedSchedule());

            // Update location
            locationDetails.setText(classModel.getLocation());
        });
    }

    /**
     * Update the students list
     */
    private void updateStudentsUI(List<Student> students) {
        runOnUiThread(() -> {
            if (students != null && !students.isEmpty()) {
                studentsAdapter.updateData(students);
            }
        });
    }

    /**
     * Update the assignments list UI
     */
    private void updateAssignmentsUI() {
        runOnUiThread(() -> {
            showLoading(false);

            if (assignmentsList.isEmpty()) {
                emptyAssignmentsText.setVisibility(View.VISIBLE);
                assignmentsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyAssignmentsText.setVisibility(View.GONE);
                assignmentsRecyclerView.setVisibility(View.VISIBLE);
                assignmentsAdapter.updateData(assignmentsList);
            }
        });
    }

    /**
     * Update the class average display
     */
    private void updateClassAverageUI(Map<String, Object> averageInfo) {
        runOnUiThread(() -> {
            if (averageInfo.containsKey("classAverage") && averageInfo.get("classAverage") != null) {
                Double classAverage = (Double) averageInfo.get("classAverage");
                classAverageText.setText(String.format("Class Avg: %.1f", classAverage));
                classAverageText.setVisibility(View.VISIBLE);
            } else {
                classAverageText.setText("Class Avg: N/A");
                classAverageText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Update the student's overall grade display
     */
    private void updateStudentOverallGradeUI(Map<String, Object> gradeInfo) {
        runOnUiThread(() -> {
            if (gradeInfo.containsKey("overallGrade") && gradeInfo.get("overallGrade") != null) {
                Double overallGrade = (Double) gradeInfo.get("overallGrade");
                studentOverallGradeText.setText(String.format("Overall Grade: %.1f", overallGrade));

                // Also show completed/total assignments
                int total = (int) gradeInfo.get("totalAssignments");
                int graded = (int) gradeInfo.get("gradedAssignments");

                studentOverallGradeText.append(String.format(" (%d/%d completed)", graded, total));
            } else {
                studentOverallGradeText.setText("Overall Grade: N/A");
            }

            studentOverallGradeText.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Show a dialog to create or edit an assignment
     */
    private void showAssignmentDialog(AssignmentModel assignment, int position) {
        boolean isNewAssignment = (assignment == null);
        boolean isTeacherGrading = !isNewAssignment && userRole.equalsIgnoreCase("teacher");
        boolean isViewOnly = !userRole.equalsIgnoreCase("teacher") ||
                (!isNewAssignment && !isTeacherGrading);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_assignment_details, null);
        builder.setView(dialogView);

        // Get dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextInputEditText nameInput = dialogView.findViewById(R.id.assignment_name_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.assignment_description_input);
        TextInputEditText weightInput = dialogView.findViewById(R.id.assignment_weight_input);

        // Grading section
        LinearLayout gradingSection = dialogView.findViewById(R.id.grading_section);
        TextInputEditText gradeInput = dialogView.findViewById(R.id.grade_input);
        TextInputEditText commentsInput = dialogView.findViewById(R.id.comments_input);

        // Info section (for view-only mode)
        LinearLayout infoSection = dialogView.findViewById(R.id.info_section);
        TextView submissionDateText = dialogView.findViewById(R.id.submission_date_text);
        TextView gradedDateText = dialogView.findViewById(R.id.graded_date_text);
        TextView gradedByText = dialogView.findViewById(R.id.graded_by_text);
        TextView commentsText = dialogView.findViewById(R.id.comments_text);

        // Student selector (for creating assignments)
        MaterialAutoCompleteTextView studentSpinner = dialogView.findViewById(R.id.student_spinner);
        View studentSpinnerLayout = dialogView.findViewById(R.id.student_spinner_layout);

        // Buttons
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Set dialog title
        if (isNewAssignment) {
            dialogTitle.setText("Create New Assignment");
        } else if (isTeacherGrading) {
            dialogTitle.setText("Grade Assignment");
        } else {
            dialogTitle.setText("Assignment Details");
        }

        // Configure student selector for new assignments
        if (isNewAssignment) {
            studentSpinnerLayout.setVisibility(View.VISIBLE);
            setupStudentSpinner(studentSpinner);
        } else {
            studentSpinnerLayout.setVisibility(View.GONE);
        }

        // Configure view mode if necessary
        if (isViewOnly) {
            // Make fields non-editable
            nameInput.setEnabled(false);
            descriptionInput.setEnabled(false);
            weightInput.setEnabled(false);
            gradingSection.setVisibility(View.GONE);

            // Show info section
            infoSection.setVisibility(View.VISIBLE);

            // Update save button
            saveButton.setText("Close");
        } else if (isTeacherGrading) {
            // For grading, only enable grade fields
            nameInput.setEnabled(false);
            descriptionInput.setEnabled(false);
            weightInput.setEnabled(false);
            gradingSection.setVisibility(View.VISIBLE);
            infoSection.setVisibility(View.GONE);
        } else {
            // For creating or editing, enable all fields
            nameInput.setEnabled(true);
            descriptionInput.setEnabled(true);
            weightInput.setEnabled(true);
            gradingSection.setVisibility(View.GONE);
            infoSection.setVisibility(View.GONE);
        }

        // Populate fields if editing an existing assignment
        if (!isNewAssignment) {
            nameInput.setText(assignment.getAssignmentName());

            if (assignment.getAssignmentDescription() != null) {
                descriptionInput.setText(assignment.getAssignmentDescription());
            }

            if (assignment.getWeightPercentage() != null) {
                weightInput.setText(String.valueOf(assignment.getWeightPercentage()));
            }

            // Set grade if available
            if (assignment.getGrade() != null) {
                gradeInput.setText(String.valueOf(assignment.getGrade()));
            }

            // Set comments if available
            if (assignment.getComments() != null) {
                commentsInput.setText(assignment.getComments());
                commentsText.setText("Comments: " + assignment.getComments());
            } else {
                commentsText.setText("Comments: None");
            }

            // Set info section text
            submissionDateText.setText("Submission Date: " + assignment.getFormattedSubmissionDate());
            gradedDateText.setText("Graded Date: " + assignment.getFormattedGradedDate());

            if (assignment.isGraded() && assignment.getGradedByName() != null) {
                gradedByText.setText("Graded By: " + assignment.getGradedByName());
            } else {
                gradedByText.setText("Graded By: N/A");
            }
        }

        // Create dialog
        AlertDialog dialog = builder.create();

        // Set button click listeners
        saveButton.setOnClickListener(v -> {
            if (isViewOnly) {
                // Just close the dialog
                dialog.dismiss();
            } else if (validateAssignmentForm(isNewAssignment, isTeacherGrading, nameInput,
                    weightInput, gradeInput, studentSpinner)) {
                // Form is valid, save the assignment
                if (isNewAssignment) {
                    createAssignment(nameInput, descriptionInput, weightInput, studentSpinner, dialog);
                } else if (isTeacherGrading) {
                    gradeAssignment(assignment, gradeInput, commentsInput, dialog);
                } else {
                    updateAssignment(assignment, nameInput, descriptionInput, weightInput, dialog);
                }
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }

    /**
     * Setup the student spinner with available students
     */
    private void setupStudentSpinner(MaterialAutoCompleteTextView studentSpinner) {
        String[] studentNames = new String[studentsList.size()];
        for (int i = 0; i < studentsList.size(); i++) {
            studentNames[i] = studentsList.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, studentNames);
        studentSpinner.setAdapter(adapter);
    }

    /**
     * Validate the assignment form
     */
    private boolean validateAssignmentForm(boolean isNewAssignment, boolean isTeacherGrading,
                                           TextInputEditText nameInput, TextInputEditText weightInput,
                                           TextInputEditText gradeInput, MaterialAutoCompleteTextView studentSpinner) {
        if (isTeacherGrading) {
            // Validate grade
            String gradeText = gradeInput.getText().toString().trim();
            if (gradeText.isEmpty()) {
                showSnackbar("Please enter a grade");
                return false;
            }

            try {
                double grade = Double.parseDouble(gradeText);
                if (grade < 0 || grade > 100) {
                    showSnackbar("Grade must be between 0 and 100");
                    return false;
                }
            } catch (NumberFormatException e) {
                showSnackbar("Please enter a valid grade");
                return false;
            }

            return true;
        } else {
            // Validate assignment details
            String name = nameInput.getText().toString().trim();
            String weightText = weightInput.getText().toString().trim();

            if (name.isEmpty()) {
                showSnackbar("Please enter an assignment name");
                return false;
            }

            if (weightText.isEmpty()) {
                showSnackbar("Please enter a weight percentage");
                return false;
            }

            try {
                double weight = Double.parseDouble(weightText);
                if (weight <= 0 || weight > 100) {
                    showSnackbar("Weight percentage must be between 0 and 100");
                    return false;
                }
            } catch (NumberFormatException e) {
                showSnackbar("Please enter a valid weight percentage");
                return false;
            }

            if (isNewAssignment && studentSpinner.getText().toString().trim().isEmpty()) {
                showSnackbar("Please select a student");
                return false;
            }

            return true;
        }
    }

    /**
     * Create a new assignment
     */
    private void createAssignment(TextInputEditText nameInput, TextInputEditText descriptionInput,
                                  TextInputEditText weightInput, MaterialAutoCompleteTextView studentSpinner,
                                  AlertDialog dialog) {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        double weight = Double.parseDouble(weightInput.getText().toString().trim());
        String studentName = studentSpinner.getText().toString().trim();

        // Find student ID
        int studentId = -1;
        for (Student student : studentsList) {
            if (student.getName().equals(studentName)) {
                studentId = student.getId();
                break;
            }
        }

        if (studentId == -1) {
            showSnackbar("Error: Could not find selected student");
            return;
        }

        // Create assignment model
        AssignmentModel newAssignment = new AssignmentModel();
        newAssignment.setClassId(classId);
        newAssignment.setClassName(currentClassModel.getClassName());
        newAssignment.setStudentId(studentId);
        newAssignment.setStudentName(studentName);
        newAssignment.setAssignmentName(name);
        newAssignment.setAssignmentDescription(description);
        newAssignment.setWeightPercentage(weight);
        newAssignment.setSubmissionDate(LocalDateTime.now());

        // Show loading
        showLoading(true);

        // Save to API
        gradesService.createAssignment(newAssignment, new GradesService.ApiCallback<AssignmentModel>() {
            @Override
            public void onSuccess(AssignmentModel result) {
                dialog.dismiss();
                showLoading(false);

                // Add to local list and update UI
                assignmentsList.add(result);
                updateAssignmentsUI();

                showSnackbar("Assignment created successfully");
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showSnackbar("Error creating assignment: " + errorMessage);
            }
        });
    }

    /**
     * Update an existing assignment
     */
    private void updateAssignment(AssignmentModel assignment, TextInputEditText nameInput,
                                  TextInputEditText descriptionInput, TextInputEditText weightInput,
                                  AlertDialog dialog) {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        double weight = Double.parseDouble(weightInput.getText().toString().trim());

        // Update assignment model
        assignment.setAssignmentName(name);
        assignment.setAssignmentDescription(description);
        assignment.setWeightPercentage(weight);

        // Show loading
        showLoading(true);

        // Save to API
        gradesService.updateAssignment(assignment, new GradesService.ApiCallback<AssignmentModel>() {
            @Override
            public void onSuccess(AssignmentModel result) {
                dialog.dismiss();
                showLoading(false);

                // Update local list and UI
                for (int i = 0; i < assignmentsList.size(); i++) {
                    if (assignmentsList.get(i).getId() == result.getId()) {
                        assignmentsList.set(i, result);
                        break;
                    }
                }

                updateAssignmentsUI();
                showSnackbar("Assignment updated successfully");
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                showSnackbar("Error updating assignment: " + errorMessage);
            }
        });
    }

    /**
     * Grade an assignment
     */
    private void gradeAssignment(AssignmentModel assignment, TextInputEditText gradeInput,
                                 TextInputEditText commentsInput, AlertDialog dialog) {
        // Get input values
        double grade = Double.parseDouble(gradeInput.getText().toString().trim());
        String comments = commentsInput.getText().toString().trim();

        // Show loading
        showLoading(true);

        // Call API to grade assignment
        gradesService.gradeAssignment(assignment.getId(), grade, userId, comments,
                new GradesService.ApiCallback<AssignmentModel>() {
                    @Override
                    public void onSuccess(AssignmentModel result) {
                        dialog.dismiss();
                        showLoading(false);

                        // Update local list and UI
                        for (int i = 0; i < assignmentsList.size(); i++) {
                            if (assignmentsList.get(i).getId() == result.getId()) {
                                assignmentsList.set(i, result);
                                break;
                            }
                        }

                        updateAssignmentsUI();

                        // Also refresh the class average
                        if (userRole.equalsIgnoreCase("teacher")) {
                            loadClassAverage();
                        }

                        showSnackbar("Assignment graded successfully");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showLoading(false);
                        showSnackbar("Error grading assignment: " + errorMessage);
                    }
                });
    }

    /**
     * Show or hide loading indicators
     */
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Show an error message dialog
     */
    private void showError(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    /**
     * Show a snackbar message
     */
    private void showSnackbar(String message) {
        runOnUiThread(() -> {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        });
    }

    /**
     * Handle grade updated event from StudentsAdapter
     */
    @Override
    public void onGradeUpdated(int studentId, String grade) {
        // Show confirmation message
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("Grade updated successfully")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    /**
     * Handle assignment click event
     */
    @Override
    public void onAssignmentClick(AssignmentModel assignment, int position) {
        showAssignmentDialog(assignment, position);
    }
}