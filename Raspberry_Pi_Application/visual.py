from graphics import *

class Visual:
    window = GraphWin("Automotive HUD", 1820, 950)
    center = Point(910,425)
    speed = Text(center, "Waiting for Connection...")
    font = "arial"
    color = "lime"
    units = " mph"

    def __init__(self):
        self.window.setBackground("black")
        self.speed.setFace(self.font)
        self.speed.setSize(100)
        self.speed.setStyle("bold")
        self.speed.setTextColor(self.color)
        self.speed.draw(self.window)
        
    def draw_speed(self, text):
        self.speed.undraw()
        self.speed = Text(self.center, text+self.units)
        self.speed.setFace(self.font)
        self.speed.setSize(300)
        self.speed.setStyle("bold")
        self.speed.setTextColor(self.color)
        self.speed.draw(self.window)

    def set_font(self, name):
        if name in ['helvetica','arial','courier','times roman']:
            self.font = name

    def set_color(self, color):
        self.color = color

    def toggle_units(self):
        if self.units == " mph":
            self.units = " kmph"
        else:
            self.units = " mph"

    def exit(self):
        self.window.close()