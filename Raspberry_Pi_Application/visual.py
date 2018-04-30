from graphics import *

class Visual:
    window = GraphWin("Automotive HUD", 1820, 950)
    center = Point(910,425)
    speed_position= Point(850,425)
    unit_position = Point(1500, 550)
    location_position = Point(910, 850)
    radio_position = Point(250, 150)
    FM_position = Point(420, 150)
    speed = Text(center, "Waiting for Connection...")
    unit = Text(unit_position, "")
    location = Text(location_position, "")
    FM = Text(FM_position,"")
    radio = Text(radio_position, "")
    unit_size = 100
    font_size = 400
    font = "arial"
    color = "lime"
    units = "MPH"
    location_shown = False 

    def __init__(self):
        self.window.setBackground("black")

        self.speed.setFace(self.font)
        self.speed.setSize(100)
        self.speed.setStyle("bold")
        self.speed.setTextColor(self.color)
        self.speed.draw(self.window)

        self.unit.setFace(self.font)
        self.unit.setSize(self.unit_size)
        self.unit.setStyle("bold")
        self.unit.setTextColor(self.color)
        self.unit.draw(self.window)

        self.radio.setFace(self.font)
        self.radio.setSize(50)
        self.radio.setStyle("bold")
        self.radio.setTextColor(self.color)
        self.radio.draw(self.window)

        self.FM.setFace(self.font)
        self.FM.setSize(50)
        self.FM.setStyle("bold")
        self.FM.setTextColor(self.color)
        self.FM.draw(self.window)
        
    def draw_speed(self, text):
        if len(text) > 3:
            return
        self.speed.undraw()
        if self.location_shown == True:
            self.location.undraw()
            self.location_shown = False
        self.speed = Text(self.speed_position, text)
        self.speed.setFace(self.font)
        self.speed.setSize(self.font_size)
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
        if color in ['White','Yellow','Blue','Lime','Red','Cyan']:
            self.color = color
            self.draw_units()
            if self.location_shown == True:
                self.draw_location(self.location.getText())
            self.draw_speed(self.speed.getText())
            self.draw_radio(self.radio.getText())

    def draw_units(self):
        self.unit.undraw()
        self.unit = Text(self.unit_position, self.units)
        self.unit.setFace(self.font)
        self.unit.setSize(self.unit_size)
        self.unit.setStyle("bold")
        self.unit.setTextColor(self.color)
        self.unit.draw(self.window)

        self.FM.undraw()
        self.FM = Text(self.FM_position,"FM")
        self.FM.setFace(self.font)
        self.FM.setSize(50)
        self.FM.setStyle("bold")
        self.FM.setTextColor(self.color)
        self.FM.draw(self.window)

    def draw_radio(self, text):
        if len(text) > 5:
            return
        self.radio.undraw()
        self.radio = Text(self.radio_position, text)
        self.radio.setFace(self.font)
        self.radio.setSize(50)
        self.radio.setStyle("bold")
        self.radio.setTextColor(self.color)
        self.radio.draw(self.window)

    def set_mph(self):
        self.units="MPH"
        self.draw_units()

    def set_kmh(self):
        self.units="km/h"
        self.draw_units()

    def exit(self):
        self.window.close()
