import java.util.List;

public class Task {
    public String description;
    public int id;
    public int startTime;
    public int duration;
    public List<Integer> comp;

    // default
    public Task() {
        description = "some description of the task.";
        id = 0;
        startTime = 0;
        duration = 0;
        comp = null;
    }

    // getters
    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public List<Integer> getComp() {
        return comp;
    }

    // setters
    public void setDescription ( String description ) {
        this.description = description;
    }

    public void setId ( int id ) {
        this.id = id;
    }

    public void setStartTime ( Integer startTime ) {
        this.startTime = startTime;
    }

    public void setDuration ( int duration ) {
        this.duration = duration;
    }

    public void setComp ( List<Integer> comp ) {
        this.comp = comp;
    }
}