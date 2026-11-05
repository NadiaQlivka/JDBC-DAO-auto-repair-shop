package autoservice.app.model;

public class Master {
  private int id;
  private String name;

  public Master(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public Master(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}