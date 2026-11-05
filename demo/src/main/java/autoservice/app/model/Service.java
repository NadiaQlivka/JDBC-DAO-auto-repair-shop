package autoservice.app.model;

import java.math.BigDecimal;

public class Service {
  private int id;
  private String name;
  private BigDecimal costOur;
  private BigDecimal costForeign;

  public Service(int id, String name, BigDecimal costOur, BigDecimal costForeign) {
    this.id = id;
    this.name = name;
    this.costOur = costOur;
    this.costForeign = costForeign;
  }

  public Service(String name, BigDecimal costOur, BigDecimal costForeign) {
    this.name = name;
    this.costOur = costOur;
    this.costForeign = costForeign;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCostOur() {
    return costOur;
  }

  public BigDecimal getCostForeign() {
    return costForeign;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCostOur(BigDecimal costOur) {
    this.costOur = costOur;
  }

  public void setCostForeign(BigDecimal costForeign) {
    this.costForeign = costForeign;
  }
}