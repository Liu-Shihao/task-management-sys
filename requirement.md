
---

## User Story 1: Excel File Upload

* **As a** FinBlock user,
* **I want to** upload Excel files directly into the system,
* **so that** I can streamline data input for my deployment tasks.

**Acceptance Criteria:**

1. Given I am on the FinBlock data input page, when I select the "Upload Excel" option, then the system prompts me to choose a file from my local machine.
2. Given I have selected a valid Excel file (.xlsx, .xls), when I confirm the upload, then the system parses the file and displays the data.
3. Given I have selected an invalid file type (e.g., .txt, .pdf), when I attempt to upload, then the system displays an error message indicating the supported file types.
4. Given the Excel file has incorrect formatting or missing data, when the system processes it, then it should display clear error messages indicating the specific issues.

---

## User Story 2: Template Management

* **As a** FinBlock user,
* **I want to** create new templates and clone existing ones,
* **so that** I can efficiently reuse and adapt deployment configurations.

**Acceptance Criteria:**

1. Given I am on the template management page, when I click the "Create New Template" button, then the system presents a form to define a new template.
2. Given I am viewing an existing template, when I click the "Clone Template" button, then the system creates a new, editable copy of that template.
3. Given I am creating or editing a template, when I save it, then it becomes available for generating release rundowns.

---

## User Story 3: Release Rundown Creation

* **As a** FinBlock user,
* **I want to** generate release rundowns from a selected template,
* **so that** I can quickly create a set of tasks for a deployment.

**Acceptance Criteria:**

1. Given I am on the release rundown creation page, when I select a template, then the system populates the rundown with the tasks defined in that template.
2. Given a release rundown is generated, then it should have a unique identifier and be in a "Pending" state.
3. Given I have generated a rundown, when I view it, then I should see the list of tasks, their sequence, and their initial status.

---

## User Story 4: Task Controls

* **As a** FinBlock user,
* **I want to** have 'Run', 'Edit', and 'Delete' controls for each task,
* **so that** I can manage the tasks in my release rundown.

**Acceptance Criteria:**

1. Given I am viewing a task in a release rundown, when I click the 'Edit' button, then I should be able to modify the task's parameters.
2. Given I am viewing a task, when I click the 'Delete' button, then the task is removed from the rundown after a confirmation prompt.
3. Given a task is in a runnable state, when I click the 'Run' button, then the 'Automated Execution' story is triggered.

---

## User Story 5: Automated Execution

* **As a** FinBlock user,
* **I want** the 'Run' button to trigger automation scripts,
* **so that** I can execute my deployment tasks without manual intervention.

**Acceptance Criteria:**

1. Given I click the 'Run' button on a task, when the task is configured for a Jenkins pipeline, then the corresponding Jenkins pipeline is triggered.
2. Given I click the 'Run' button on a task, when the task is configured for an Ansible job, then the corresponding Ansible job is launched.
3. Given a task is running, when its status changes (e.g., 'In Progress', 'Completed', 'Failed'), then the status is updated and displayed in the FinBlock UI in near real-time and provide the Jenkins build URL/Ansible job URL.
4. Given an automation script fails, when the task status is updated to 'Failed', then any error messages or logs from the script are accessible from the task in FinBlock.

---


## 技术栈

- 使用SpringBoot开发
- Java21
- Maven
- H2(内存数据库)
- JPA