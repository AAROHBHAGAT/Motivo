import java.io.Serializable;

public class Task implements Serializable {
    private String description;
    private String dueDate;
    private boolean isComplete;

    public Task(String description, String dueDate) {
        this.description = description;
        this.dueDate = dueDate;
        this.isComplete = false;
    }

    public void markComplete() {
        this.isComplete = true;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDueDate() {
        return this.dueDate;
    }

    public String toString() {
        return (isComplete ? "[X] " : "[ ] ") + description +
                (dueDate != null && !dueDate.isEmpty() ? " (Due: " + dueDate + ")" : "");
    }
}
