class Constants_Enum:
    TIME_TO_REMOVE_MSG = 24


class group_type_Enum:
    static = 0
    dynamic = 1


class gender_Enum:
    men = 0
    women = 1

class RequestAnswer:
    ignore = 0
    accept = 1


class message_type_Enum:
    private = 0  # to a private user
    public = 1  # to a group


class Location(object):
    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

    def __str__(self):
        return str(self.latitude) + "|" + str(self.longitude)

    def __repr__(self):
        return self.__str__()




# while creating the object, the server thread will get all the data from the global dictionaries
# if it is a new user, the server will put null in the dictionaries and also will add to the global dictionaries
class User(object):
    def __init__(self, username, md5_pw, location, fname, lname, address, gender, phone_num, email, pic_data,
                 is_connected, l_seen, user_groups,
                 hidden_groups, manage_groups, friends, users_who_asked_request, users_who_will_answer_request):
        self.username = username
        self.md5_pw = md5_pw
        self.location = location
        self.fname = fname
        self.lname = lname
        self.address = address
        self.gender = gender
        self.phone_num = phone_num
        self.email = email
        self.pic_data = pic_data
        self.is_connected = is_connected  # to the application
        self.l_seen = l_seen  # the server will change this just after the client will disconnect him

        self.user_groups = user_groups  # dictionary of groups - group_id:group - the user can only see the user groups in his location
        self.hidden_groups = hidden_groups  # dictionary of groups - group_id:group - the user can only see the hidden groups in his location
        self.manage_groups = manage_groups  # dictionary of groups - group_id:group - the user can only see the manager groups in his location

        self.friends = friends  # dictionary of users - user_id_id:user
        self.users_who_asked_request = users_who_asked_request  # dictionary of users - user_id_id:user
        self.users_who_will_answer_request = users_who_will_answer_request  # dictionary of users - user_id_id:user

    def send_a_message(self, message, dst_id_group, sock):
        pass

    def __str__(self):
        #return '~'.join(
        #    [self.username, self.md5_pw, str(self.location), self.fname, self.lname, self.address, str(self.gender),
        #     str(self.phone_num),
        #     self.email, self.pic_data, self.l_seen, str(self.is_connected), str(self.user_groups.values()),
        #     str(self.hidden_groups.values()),
        #    str(self.manage_groups.values()), '!'.join(self.friends.values()), '!'.join(self.users_who_asked_request.values()),
        #     '!'.join((self.users_who_will_answer_request.values()))])

        return '~'.join(
            [self.username, self.md5_pw, str(self.location), self.fname, self.lname, self.address, str(self.gender),
             str(self.phone_num),
             self.email, self.pic_data, self.l_seen, str(self.is_connected), str(self.user_groups.values()),
             str(self.hidden_groups.values()),
             str(self.manage_groups.values()), '!'.join([str(x) for x in self.friends.values()]),
             '!'.join([str(x) for x in self.users_who_asked_request.values()]),
             '!'.join([str(x) for x in  self.users_who_will_answer_request.values()])])

    def __repr__(self):
        return self.__str__()



#all the users that are not the user of the son thread.
#mainly to prevent recurtion while trying to get to the groups of the servers son user.
class OtherUser(object):
    def __init__(self, username, md5_pw, location, fname, lname, address, gender, phone_num, email, pic_data,
                 is_connected, l_seen):
        self.username = username
        self.md5_pw = md5_pw
        self.location = location
        self.fname = fname
        self.lname = lname
        self.address = address
        self.gender = gender
        self.phone_num = phone_num
        self.email = email
        self.pic_data = pic_data
        self.l_seen = l_seen  # the server will change this just after the client will disconnect him
        self.is_connected = is_connected  # to the application


    def __str__(self):
        return '#'.join(
            [self.username, self.md5_pw, str(self.location), self.fname, self.lname, self.address, str(self.gender),
             str(self.phone_num),
             self.email, self.pic_data, self.l_seen, str(self.is_connected)])

    def __repr__(self):
        return self.__str__()






class Group(object):
    def __init__(self, id, type, middle_location, radius, name, pic_data, manager_user_id, users, messages, time_created):
        self.id = id
        self.type = type
        self.mid_loc = middle_location
        self.radius = radius
        self.name = name
        self.pic_data = pic_data
        self.manager_user_id = manager_user_id  # OtherUser
        self.users = users #OtherUser object
        self.messages = messages
        self.time_created = time_created #string
        self.time_to_remove_msg = Constants_Enum.TIME_TO_REMOVE_MSG #int - 24 hours

    def __str__(self):

        '''
        users_str = ''
        for other_user in self.users.values():
            users_str += str(other_user) + "!"
        users_str= users_str[:len(users_str)-1]
        '''
        users_str = '!'.join([str(x) for x in self.users.values()])  # list comprehension
        #'!'.join(str(x) for x in self.users.values()) # generator expression
        #users_str = '!'.join(self.users.values())
        if users_str == '':
            users_str = '-'
        #messages_str = '!'.join(self.messages)
        messages_str = '!'.join([str(x) for x in self.messages])
        if messages_str == '':
            messages_str = '-'
        return "&".join([str(self.id), str(self.type), str(self.mid_loc), str(self.radius), self.name, self.pic_data,
                         self.manager_user_id, self.time_created, str(self.time_to_remove_msg), users_str, messages_str])
#change null to '-'
    def __repr__(self):
        return self.__str__()


class Message(object):
    def __init__(self, id, type, group_id, src_user_id, dst_id, send_time, data):
        self.id = id
        self.type = type  # private/public
        self.group_id = group_id
        self.src_user_id = src_user_id  # only user
        self.dst_id = dst_id  # can be a user or a group
        self.send_time = send_time #string
        self.data = data

    def __str__(self):
        return "#".join([str(self.id),str(self.type),self.group_id ,self.src_user_id, self.dst_id, self.send_time, self.data])

    def __repr__(self):
        return self.__str__()

