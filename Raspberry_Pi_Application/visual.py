from graphics import *

class Visual:
    window = GraphWin("Automotive HUD", 1820, 950)
    center = Point(910,425)
    speed_position= Point(850,425)
    unit_position = Point(1365, 550)
    location_position = Point(910, 850)
    speed = Text(center, "Waiting for Connection...")
    unit = Text(unit_position, "")
    location = Text(location_position, "")
    font = "arial"
    color = "lime"
    units = "mph"
    location_shown = False 

    def __init__(self):
        self.window.setBackground("black")

        self.speed.setFace(self.font)
        self.speed.setSize(100)
        self.speed.setStyle("bold")
        self.speed.setTextColor(self.color)
        self.speed.draw(self.window)

        self.unit.setFace(self.font)
        self.unit.setSize(50)
        self.unit.setStyle("bold")
        self.unit.setTextColor(self.color)
        self.unit.draw(self.window)
        
    def draw_speed(self, text):
        self.speed.undraw()
        if self.location_shown == True:
            self.location.undraw()
            self.location_shown = False
        self.speed = Text(self.speed_position, text)
        self.speed.setFace(self.font)
        self.speed.setSize(300)
        self.speed.setStyle("bold")
        self.speed.setTextColor(self.color)
        self.speed.draw(self.window)

    def draw_location(self, text):
        if self.location_shown == True:
            self.location.undraw()
        self.location_shown = True 
        self.location= Text(self.location_position, "Current Location:\n"+text)
        self.location.setFace(self.font)
        self.location.setSize(50)
        self.location.setStyle("bold")
        self.location.setTextColor(self.color)
        self.location.draw(self.window)

    def set_color(self, color):
        self.color = color
        self.draw_units()
        if self.location_shown == True:
            self.draw_location(self.location.getText())
        self.draw_speed(self.speed.getText())

    def draw_units(self):
        self.unit.undraw()
        self.unit = Text(self.unit_position, self.units)
        self.unit.setFace(self.font)
        self.unit.setSize(50)
        self.unit.setStyle("bold")
        self.unit.setTextColor(self.color)
        self.unit.draw(self.window)

    def set_mph(self):
        self.units="mph"
        self.draw_units()

    def set_kmh(self):
        self.units="km/h"
        self.draw_units()

    def exit(self):
        self.window.close()
