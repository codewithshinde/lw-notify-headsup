package com.lwnotifyheadsup;

public enum NotifyOptions {

  CUSTOM_COMPONENT("customComponent");

  private final String option;

  NotifyOptions(String option) {
    this.option = option;
  }

  public String getOption() {
    return option;
  }
}
