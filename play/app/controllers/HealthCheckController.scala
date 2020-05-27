package controllers

import play.api.mvc.InjectedController

class HealthCheckController extends InjectedController {

  def ok(all: String) = Action(Ok)
}
