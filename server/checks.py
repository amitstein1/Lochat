class Location(object):
    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

    def __str__(self):
        return str(self.latitude) + "|" + str(self.longitude)

    def __repr__(self):
        return self.__str__()


    def a(self):
        return self

loc = Location(45,78)
other_loc = loc.a()
print other_loc