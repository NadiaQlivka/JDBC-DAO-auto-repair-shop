package autoservice.app.model;

public class Car {
  private int id;
  private String num;
  private String color;
  private String mark;
  private boolean isForeign;

  public Car(int id, String num, String color, String mark, boolean isForeign) {
    this.id = id;
    this.num = num;
    this.color = color;
    this.mark = mark;
    this.isForeign = isForeign;
  }

  public Car(String num, String color, String mark, boolean isForeign) {
    this.num = num;
    this.color = color;
    this.mark = mark;
    this.isForeign = isForeign;
  }

  public int getId() {
    return id;
  }

  public String getNum() {
    return num;
  }

  public String getColor() {
    return color;
  }

  public String getMark() {
    return mark;
  }

  public boolean isForeign() {
    return isForeign;
  }

  public void setNum(String num) {
    this.num = num;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setMark(String mark) {
    this.mark = mark;
  }

  public void setIsForeign(boolean isForeign) {
    this.isForeign = isForeign;
  }
}
