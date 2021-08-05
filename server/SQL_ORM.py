import sqlite3
import json
import pickle

# import self as self

import classes

# https://docs.python.org/2/library/sqlite3.html
# https://www.youtube.com/watch?v=U7nfe4adDw8


__author__ = 'amit'


class MyDataBaseORM(object):
    def __init__(self):
        self.conn = None  # will store the DB connection
        self.cursor = None  # will store the DB connection cursor
        self.current = None
        self.data_base_name = "MyDataBase.db"  # const

        self.open_DB()

        # user_id is username
        sql = 'CREATE TABLE IF NOT EXISTS Users (user_id TEXT, md5_pw TEXT, latitude REAL, longitude REAL, fname TEXT, ' \
              'lname TEXT, address TEXT, gender INT, phone_num INT,' \
              ' email TEXT, pic_data TEXT, is_connected BIT, l_seen TEXT)'
        self.current.execute(sql)
        sql = 'CREATE TABLE IF NOT EXISTS Groups (group_id TEXT, type TEXT, name TEXT, latitude REAL, longitude REAL, ' \
              'radius REAL, pic_data TEXT, manager_id TEXT, time_created TEXT) '
        self.current.execute(sql)
        sql = 'CREATE TABLE IF NOT EXISTS UserGroup (user_id TEXT, group_id TEXT, is_hidden INT, is_manager INT)'
        self.current.execute(sql)
        sql = 'CREATE TABLE IF NOT EXISTS Messages (msg_id TEXT,type TEXT, group_id TEXT, sender_id TEXT, receiver_id TEXT, time TEXT, data TEXT) '
        self.current.execute(sql)

        sql = 'CREATE TABLE IF NOT EXISTS OpenRequests (sender_id TEXT, receiver_id TEXT)'
        self.current.execute(sql)

        sql = 'CREATE TABLE IF NOT EXISTS UserFriends (user_id TEXT, friend_id TEXT)'
        self.current.execute(sql)

        # sql = 'CREATE TABLE IF NOT EXISTS Constants (name TEXT, value TEXT)'
        # self.current.execute(sql)

        sql = 'CREATE TABLE IF NOT EXISTS OpenValues (name TEXT, value TEXT)'
        self.current.execute(sql)

        self.commit()
        self.close_DB()

    def open_DB(self):
        """
        will open DB file and put value in:
        self.conn (need DB file name)
        and self.cursor
        """
        self.conn = sqlite3.connect(self.data_base_name)
        # self.conn.text_factory = str
        self.current = self.conn.cursor()

    def close_DB(self):
        self.conn.close()

    def commit(self):
        """
            saves the data in the db file
        """
        self.conn.commit()

    def insert_new_user_to_users(self, user):
        try:
            self.open_DB()
            sql = "INSERT INTO Users (user_id, md5_pw, latitude, longitude, fname, lname, address, gender, phone_num, email, pic_data, is_connected, l_seen) "

            # sql += " VALUES('" + user.username + "', '" + user.md5_pw + "', " + user.location.latitude + ", " + user.location.longitude \
            #    + ", '" + user.fname + "', '" + user.lname + "', '" + user.address + "', " + user.phone_num + ", '" + user.email + "', '"\
            #  + user.pic_data + "', " + str(1) + ", '" + user.l_seen + "');"
            sql += "VALUES('{}', '{}', {}, {}, '{}', '{}', '{}', {}, {}, '{}', '{}', {}, '{}');".format(user.username,
                                                                                                        user.md5_pw,
                                                                                                        user.location.latitude,
                                                                                                        user.location.longitude,
                                                                                                        user.fname,
                                                                                                        user.lname,
                                                                                                        user.address,
                                                                                                        str(
                                                                                                            user.gender),
                                                                                                        user.phone_num,
                                                                                                        user.email,
                                                                                                        user.pic_data,
                                                                                                        str(1),
                                                                                                        user.l_seen)
            # user.is_connected
            # sql = 'CREATE TABLE IF NOT EXISTS Users (user_id TEXT, md5_pw TEXT, latitude INT, longitude INT, fname TEXT, ' \
            #     'lname TEXT, address TEXT, gender INT, phone_num INT,' \
            #     ' email TEXT, pic_data TEXT, is_connected BIT, l_seen TEXT)'
            print "sql >>> " + sql
            self.current.execute(sql)  # res = self.current.execute(sql)
            self.commit()
            print "user inserted to users"
        except Exception as e:
            print str(e)
        finally:
            self.close_DB()

    def is_user_in_group(self, user_id, group_id):
        db_user_groups = self.get_db_user_groups(user_id)
        return group_id in db_user_groups
    def get_all_user_groups(self, user_id):
        try:
            self.open_DB()
            sql = "SELECT * FROM UserGroup where user_id = '" + user_id + "';"
            result = self.current.execute(sql)
            print "result=" + str(result)
            user_groups = result.fetchall()

            self.close_DB()
            return user_groups
        except Exception as e:
            print "error get user groups:" + e

    def get_db_user_groups(self, user_id):
        try:
            self.open_DB()
            sql = "SELECT group_id FROM UserGroup where user_id = '" + user_id + "';"
            result = self.current.execute(sql)
            print "result=" + str(result)
            user_groups = result.fetchall()

            self.close_DB()
            return user_groups
        except Exception as e:
            print "error get user groups:" + e

    # insert the user to the UserGroup table with each of his groups separately the method will not insert user
    # and group that are already exist - the function will not delete groups that the user is not in them
    def insert_user_to_his_groups(self, user_id, user_groups):
        try:
            for group_id in user_groups:
                if not self.is_user_in_group(user_id, group_id):
                    self.insert_user_group(user_id,group_id)
                    self.add_user_group(user_id,group_id,False,False)
                    '''self.open_DB()
                    sql = "INSERT INTO UserGroup (user_id, groups_id)"
                    sql += " VALUES(" + user_id + ", '" + group_id + ");"
                    print "sql >>> " + sql
                    self.current.execute(sql)  # res = self.current.execute(sql)
                    # the user have no friends, requests or messages
                    self.commit()
                    self.close_DB()'''
        except:
            print "error insert user to groups"

            # the function will not delete groups that the user is not in them



    def insert_user_group(self, user_id, group_id):
        try:
            self.open_DB()
            sql = "INSERT INTO UserGroup (user_id, groups_id)"
            sql += " VALUES(" + user_id + ", '" + group_id + ");"
            print "sql >>> " + sql
            self.current.execute(sql)  # res = self.current.execute(sql)
            # the user have no friends, requests or messages
            self.commit()

        except Exception as e:
            print e
        finally:
            self.close_DB()

    def remove_user_group(self, user_id, group_id):
        try:
            self.open_DB()
            sql = "DELETE FROM UserGroup where user_id =" + user_id + " AND group_id = " + group_id + ";"
            res = self.current.execute(sql)
            self.commit()
            result = res.fetchall()
            print "result=" + str(result)

        except Exception as e:
            print e
        finally:
            self.close_DB()


    def remove_user_groups_that_user_not_in_them(self, user_id, user_groups_id):
        db_groups_id = self.get_db_user_groups(user_id)
        print db_groups_id
        for group_id in db_groups_id:
            if group_id not in user_groups_id:
                self.remove_user_group(user_id, group_id)

    def get_all_users(self):  # groups to add the users the lists of groups
        try:
            users = {}

            self.open_DB()
            sql = "SELECT user_id FROM Users;"
            sql_result = self.current.execute(sql)
            list_tuple_users_ids = sql_result.fetchall()
            list_users_ids = [str(x[0]) for x in list_tuple_users_ids]
            print "all user ids=" + str(list_users_ids)
            self.close_DB()

            for user_id in list_users_ids:
                new_user = self.get_user_object_by_id(user_id)
                # if user_id not in users.keys():
                #    users[user_id] = new_user
                try:
                    users[user_id] = new_user
                except:
                    print "bad - there are two or more users with the same user name!!!!!!!"
            return users
        except Exception as e:
            print "error get user groups:" + str(e)

    def get_all_groups(self):
        self.open_DB()
        sql = "SELECT group_id FROM Groups;"
        sql_result = self.current.execute(sql)
        id_tuples_list = sql_result.fetchall()
        groups_ids = []
        for id_tuple in id_tuples_list:
            groups_ids.append(str(id_tuple[0]))
        print "groups_ids=" + str(groups_ids)
        self.close_DB()
        all_groups = {}
        for group_id in groups_ids:
            new_group_obj = self.get_group_object_by_group_id(group_id)
            all_groups[group_id] = new_group_obj
        return all_groups

    def get_user_object_by_id(self, user_id):
        self.open_DB()
        sql = "SELECT * FROM Users where user_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        list_user = sql_result.fetchall()[0]
        print "list_user_props=" + str(list_user)
        print "ha"
        username, md5_pw, latitude, longitude, fname, lname, address, gender, phone_num, email, pic_data, is_connected, l_seen = \
            str(list_user[0]), str(list_user[1]), str(list_user[2]), str(list_user[3]), str(list_user[4]), str(
                list_user[5]), \
            str(list_user[6]), str(list_user[7]), str(list_user[8]), str(list_user[9]), list_user[10], str(
                list_user[11]), \
            str(list_user[12])
        #########
        # actually does not matter because this information will change immediatly...
        latitude = float(latitude)
        longitude = float(longitude)
        location = classes.Location(longitude, longitude)
        #########
        phone_num = int(phone_num)
        gender = int(gender)
        if is_connected == '0':
            is_connected = False
        else:
            is_connected = True
        print "params for user no lists=" + ','.join(
            [username, md5_pw, str(latitude), str(longitude), fname, lname, address, str(gender),
             str(phone_num), email, pic_data, str(is_connected), l_seen])
        self.close_DB()
        # this dict changes every second so we do not need to get it from here, after the user autentication his son server will check his groups by location
        user_groups, hidden_groups, manage_groups = self.get_all_user_Group_dicts_by_id(user_id)  # dict - not list

        friends = self.get_friends_by_user_id(user_id)
        users_who_asked_request = self.get_users_who_asked_request_by_user_id(user_id)
        users_who_will_answer_request = self.get_users_who_will_answer_request_by_user_id(user_id)

        new_user = classes.User(username, md5_pw, location, fname, lname, address, gender, phone_num, email, pic_data,
                                is_connected, l_seen, user_groups, hidden_groups, manage_groups,
                                friends, users_who_asked_request, users_who_will_answer_request)
        return new_user

    def get_friends_by_user_id(self, user_id):
        self.open_DB()
        sql = "SELECT friend_id FROM UserFriends where user_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        friends_ids = sql_result.fetchall()
        print "friends_ids=" + str(friends_ids)
        self.close_DB()
        friends = {}
        for friend_id in friends_ids:
            friend_id = str(friend_id)
            friend_obj = self.get_OtherUser_object_by_user_id(friend_id)
            friends[friend_id] = friend_obj
        return friends

    def get_users_who_asked_request_by_user_id(self, user_id):
        self.open_DB()
        sql = "SELECT sender_id FROM OpenRequests where receiver_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        users_who_asked_requests_id = sql_result.fetchall()
        print "users_who_will_answer_requests_id=" + str(users_who_asked_requests_id)
        self.close_DB()
        users_who_asked_request = {}
        for user_who_will_answer_request_id in users_who_asked_requests_id:
            user_who_will_answer_request_id = str(user_who_will_answer_request_id)
            user_who_will_answer_request_obj = self.get_OtherUser_object_by_user_id(user_who_will_answer_request_id)
            users_who_asked_request[user_who_will_answer_request_id] = user_who_will_answer_request_obj
        return users_who_asked_request

    def get_users_who_will_answer_request_by_user_id(self, user_id):
        self.open_DB()
        sql = "SELECT receiver_id FROM OpenRequests where sender_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        users_who_will_answer_requests_id = sql_result.fetchall()
        print "users_who_will_answer_requests_id=" + str(users_who_will_answer_requests_id)
        self.close_DB()
        users_who_will_answer_request = {}
        for user_who_will_answer_request_id in users_who_will_answer_requests_id:
            user_who_will_answer_request_id = str(user_who_will_answer_request_id)
            user_who_will_answer_request_obj = self.get_OtherUser_object_by_user_id(user_who_will_answer_request_id)
            users_who_will_answer_request[user_who_will_answer_request_id] = user_who_will_answer_request_obj
        return users_who_will_answer_request

    def get_group_object_by_group_id(self, group_id):
        self.open_DB()
        sql = "SELECT * FROM Groups where group_id ='" + group_id + "';"
        sql_result = self.current.execute(sql)
        list_group_props = sql_result.fetchall()[0]
        print "list_group_props=" + str(list_group_props)
        self.close_DB()
        id, type, name, latitude, longitude, radius, pic_data, manager_user_id, time_created = \
            str(list_group_props[0]), str(list_group_props[1]), str(list_group_props[2]), str(list_group_props[3]), str(
                list_group_props[4]), \
            list_group_props[5], \
            str(list_group_props[6]), str(list_group_props[7]), str(list_group_props[8])
        type = int(type)
        latitude = float(latitude)
        longitude = float(longitude)
        mid_loc = classes.Location(latitude, longitude)
        radius = float(radius)
        print "params for group without the lists=" + ','.join(
            [id, str(type), str(latitude), str(longitude), str(radius), name, pic_data, manager_user_id, time_created])

        users = self.get_all_users_by_group_id(group_id)
        messages = self.get_all_mesaages_by_group_id(group_id)  #

        new_group = classes.Group(id, type, mid_loc, radius, name, pic_data, manager_user_id, users,
                                  messages, time_created)

        return new_group

    def get_all_users_by_group_id(self, group_id):
        # OtherUsers!!!! #all the users that are in the group - get all users and then get just the users (all props)
        # theat are in the group by location function - copy from server program
        self.open_DB()
        sql = "SELECT user_id FROM UserGroup where group_id ='" + group_id + "';"
        sql_result = self.current.execute(sql)
        list_users_ids = list(sql_result.fetchall()[0])
        print "list_users_ids=" + str(list_users_ids)
        print 11111111111111111111
        self.close_DB()

        all_users = {}
        for user_id in list_users_ids:
            other_user_obj = self.get_OtherUser_object_by_user_id(str(user_id))
            all_users[other_user_obj.username] = other_user_obj
        return all_users

    def get_OtherUser_object_by_user_id(self, user_id):
        self.open_DB()
        sql = "SELECT * FROM Users where user_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        list_user = list(sql_result.fetchall()[0])
        print "list_OtherUser_props=" + str(list_user)
        self.close_DB()

        username, md5_pw, latitude, longitude, fname, lname, address, gender, phone_num, email, pic_data, is_connected, l_seen = \
            str(list_user[0]), str(list_user[1]), str(list_user[2]), str(list_user[3]), str(list_user[4]), str(
                list_user[5]), \
            str(list_user[6]), str(list_user[7]), str(list_user[8]), str(list_user[9]), list_user[10], str(
                list_user[11]), \
            str(list_user[12])
        latitude = float(latitude)
        longitude = float(longitude)
        location = classes.Location(latitude, longitude)
        phone_num = int(phone_num)
        gender = int(gender)
        if is_connected == '0':
            is_connected = False
        else:
            is_connected = True
        print "params for OtherUser no lists=" + ','.join(
            [username, md5_pw, str(latitude), str(longitude), fname, lname, address, str(gender),
             str(phone_num), email, pic_data, str(is_connected), l_seen])

        new_other_user = classes.OtherUser(username, md5_pw, location, fname, lname, address, gender, phone_num, email,
                                           pic_data, is_connected, l_seen)
        return new_other_user

    def get_all_mesaages_by_group_id(self, group_id):
        # get all messages from messages table where the group id of the message is right
        # then sort the messeges by their end time (maybe there is a function already - try google)
        # then return the messages as a list!! - not dictionary
        self.open_DB()
        sql = "SELECT msg_id FROM Messages where group_id ='" + group_id + "';"
        sql_result = self.current.execute(sql)
        list_tuple_messages_ids = sql_result.fetchall()
        list_messages_ids = [str(x[0]) for x in list_tuple_messages_ids]
        print "all messages ids=" + str(list_messages_ids)
        self.close_DB()

        all_messages = []
        for msg_id in list_messages_ids:
            message_obj = self.get_message_object_by_msg_id(msg_id)
            all_messages.append(message_obj)

        sorted_all_messages = sorted(all_messages, key=lambda x: int(x.id), reverse=False)
        # must because the mesages are in the database randomly
        # we can also sort by the sendin time - harder
        return sorted_all_messages

    def get_message_object_by_msg_id(self, msg_id):
        self.open_DB()
        sql = "SELECT * FROM Messages where msg_id ='" + msg_id + "';"
        sql_result = self.current.execute(sql)
        list_msg_props = list(sql_result.fetchall()[0])
        print "list_Message_props=" + str(list_msg_props)
        self.close_DB()
        msg_id, msg_type, group_id, sender_id, receiver_id, time, data = str(list_msg_props[0]), str(
            list_msg_props[1]), str(list_msg_props[2]), str(list_msg_props[3]), str(list_msg_props[4]), str(
            list_msg_props[5]), str(list_msg_props[6])
        msg_type = int(msg_type)

        print "params for Message=" + ','.join([msg_id, str(msg_type), sender_id, group_id, receiver_id, time, data])
        new_Message = classes.Message(msg_id, msg_type, group_id, sender_id, receiver_id, time, data)

        return new_Message

    # we calll tjis function only at the beginning - when the server wants to import all the data
    def get_all_user_Group_dicts_by_id(self, user_id):
        self.open_DB()
        sql = "SELECT * FROM UserGroup where user_id ='" + user_id + "';"
        sql_result = self.current.execute(sql)
        list_groups_3_props = sql_result.fetchall()  # list of tuples!
        print "list_groups_3_props=" + str(list_groups_3_props)
        self.close_DB()
        user_groups, hidden_groups, manage_groups = {}, {}, {}
        for group_tuple in list_groups_3_props:
            # user_id= str(group[0])
            group_id = str(group_tuple[1])
            is_hidden = int(str(group_tuple[2]))
            is_manager = int(str(group_tuple[3]))

            group_obj = self.get_group_object_by_group_id(group_id)
            # we will add groups only to manage_groups and hidden_groups
            # because these lists do not change every second by the location
            # but the user won't be able to open them if he is nit in their location!
            if is_manager == 1:
                manage_groups[group_id] = group_obj

            if is_hidden == 1:
                hidden_groups[group_id] = group_obj
            # else:
            #    user_groups[group_id] = group_obj

        return user_groups, hidden_groups, manage_groups
        # return user_groups, hidden_groups, manage_groups

    def get_open_message_id(self):
        self.open_DB()
        sql = "SELECT value FROM OpenValues where name = " + "'open_message_id'" + ";"

        sql_result = self.current.execute(sql)
        l_open_message_id = sql_result.fetchall()
        if len(l_open_message_id) == 0:
            print "first time of the server????"
            open_message_id = "0"
            sql = "INSERT INTO OpenValues (name, value) "
            sql += "VALUES ('{}', '{}');".format("open_message_id", open_message_id)
            print "sql >>> " + sql
            res = self.current.execute(sql)
            self.commit()
            self.close_DB()
            print "now message id =" + open_message_id
            return open_message_id
        else:
            open_message_id = str(l_open_message_id[0][0])
            print "now message id =" + open_message_id
            self.close_DB()
            return open_message_id

    def set_open_message_id(self, new_message_id):
        self.open_DB()
        sql = "UPDATE OpenValues SET value ='" + new_message_id + "' where name = 'open_message_id';"
        sql_result = self.current.execute(sql)
        self.commit()
        sql = "SELECT value FROM OpenValues where name = 'open_message_id';"
        sql_result = self.current.execute(sql)
        l_new_message_id = sql_result.fetchall()
        print "open message id updated to=" + str(l_new_message_id)

        self.close_DB()

    def add_new_message(self, new_message):
        print "new message will be add to db=" + str(new_message)
        try:
            self.open_DB()

            sql = "INSERT INTO Messages (msg_id, type, group_id, sender_id, receiver_id, time, data) "
            sql += "VALUES('{}', '{}', '{}', '{}', '{}', '{}', '{}');".format(new_message.id,
                                                                              str(new_message.type),
                                                                              new_message.group_id,
                                                                              new_message.src_user_id,
                                                                              new_message.dst_id,
                                                                              new_message.send_time, new_message.data)
            print "sql >>> " + sql
            self.current.execute(sql)
            self.commit()
            print "message was inserted to Messages"
            print "all messages in group=" + str(self.get_all_mesaages_by_group_id(new_message.group_id))
        except Exception as e:
            print str(e)
        finally:
            self.close_DB()

    def get_open_group_id(self):
        self.open_DB()
        sql = "SELECT value FROM OpenValues where name = " + "'open_group_id'" + ";"

        sql_result = self.current.execute(sql)
        l_open_group_id = sql_result.fetchall()
        if len(l_open_group_id) == 0:
            print "first time of the server????"
            open_group_id = "0"
            sql = "INSERT INTO OpenValues (name, value) "
            sql += "VALUES ('{}', '{}');".format("open_group_id", open_group_id)
            print "sql >>> " + sql
            res = self.current.execute(sql)
            self.commit()
            self.close_DB()
            return open_group_id
        else:
            open_group_id = str(l_open_group_id[0][0])

        self.close_DB()
        return open_group_id

    def set_open_group_id(self, new_group_id):
        self.open_DB()
        sql = "UPDATE OpenValues SET value ='" + new_group_id + "' where name = 'open_group_id';"
        sql_result = self.current.execute(sql)
        self.commit()
        sql = "SELECT value FROM OpenValues where name = 'open_group_id';"
        sql_result = self.current.execute(sql)
        l_open_group_id = sql_result.fetchall()
        print "open group id updated to=" + str(l_open_group_id)

        self.close_DB()

    def add_new_group(self, new_group):
        print "new group will be add to db=" + str(new_group)
        try:
            self.open_DB()
            sql = "INSERT INTO Groups (group_id, type, name, latitude, longitude, radius, pic_data, manager_id, time_created) "
            sql += "VALUES('{}', {}, '{}', {}, {}, {}, '{}', '{}', '{}');".format(new_group.id,
                                                                                  str(new_group.type), new_group.name,
                                                                                  str(new_group.mid_loc.latitude),
                                                                                  str(new_group.mid_loc.longitude),
                                                                                  str(new_group.radius),
                                                                                  new_group.pic_data,
                                                                                  new_group.manager_user_id,
                                                                                  new_group.time_created)
            print "sql >>> " + sql
            self.current.execute(sql)
            self.commit()
            print "group was inserted to Groups"
        except Exception as e:
            print str(e)
        finally:
            self.close_DB()

    def add_user_group(self, user_id, group_id, is_hidden, is_manager):
        try:
            if is_hidden:
                is_hidden = 1
            else:
                is_hidden = 0
            if is_manager:
                is_manager = 1
            else:
                is_manager = 0
            self.open_DB()
            sql = "INSERT INTO UserGroup (user_id, group_id, is_hidden, is_manager) "
            sql += "VALUES('{}', '{}', {}, {});".format(user_id, group_id, is_hidden, is_manager)
            print "sql >>> " + sql
            self.current.execute(sql)
            self.commit()
            print "user_group was inserted to UserGroups"
        except Exception as e:
            print str(e)
        finally:
            self.close_DB()

    def hide_user_group(self, user_id, group_id):
        self.open_DB()
        sql = "UPDATE UserGroup SET is_hidden = 1 where user_id = '" + user_id + "' AND group_id = '" + group_id + "';"
        sql_result = self.current.execute(sql)
        self.commit()
        sql = "SELECT is_hidden FROM UserGroup where user_id = '" + user_id + "' AND group_id = '" + group_id + "';"
        sql_result = self.current.execute(sql)
        is_hidden = sql_result.fetchall()
        print "is_hidden updated to=" + str(is_hidden)
        self.close_DB()

    def unhide_user_group(self, user_id, group_id):
        self.open_DB()
        sql = "UPDATE UserGroup SET is_hidden = 0 where user_id = '" + user_id + "' AND group_id = '" + group_id + "';"
        sql_result = self.current.execute(sql)
        self.commit()
        sql = "SELECT is_hidden FROM UserGroup where user_id = '" + user_id + "' AND group_id = '" + group_id + "';"
        sql_result = self.current.execute(sql)
        is_hidden = sql_result.fetchall()
        print "is_hidden updated to=" + str(is_hidden)
        self.close_DB()

    # def get_group_of_user(self, group_id):
    #    pass


'''
    def GetNameByID(self, id):
        self.open_DB()
        sql = "SELECT name FROM owners WHERE id =" + str(id)
        res = self.current.execute(sql)
        self.close_DB()
        return res.fetchall()[0]

    
    def insert_new_owner(self,name,age):
        self.open_DB()
        sql= "SELECT MAX(id) FROM owners"
        res = self.current.execute(sql)
        for ans in res:
            accountID= ans[0]+1
        sql="INSERT INTO owners (id,name,age)"
        sql+=" VALUES("+str(accountID)+","+"'"+name+"'," + str(age) +")"
        res =self.current.execute(sql)

        self.commit()
        self.close_DB()
        print res
        return "Ok"


         
        self.close_DB()
        return res
    

    def get_all_students(self):
        try:
            self.open_DB()
            sql = "SELECT * FROM students;"
            res = self.current.execute(sql)
            res = res.fetchall()  # all row in a cell inside a list converted with json to string
            self.close_DB()
            return res
        except:
            return "failed"

    def get_all_owner_contacts_by_name(self, owner_first_name, owner_last_name):
        try:
            self.open_DB()
            sql = "SELECT id FROM owners where first_name = '" + owner_first_name + "' AND last_name = '" + owner_last_name + "';"

            res = self.current.execute(sql)
            id = res.fetchall()[0]
            print "owner id=" + str(id[0])

            sql = "SELECT first_Name, last_Name, address, phone, email	FROM contacts WHERE id = " + str(id[0]) + ";"
            # sql+= "LEFT OUTER join owners on (owner_id	 = " + str(id[0])+")"
            ret = self.current.execute(sql)
            # self.commit()
            result = json.dumps(ret.fetchall())
            # result = ret.fetchall()
            print "result=" + result
            return result
        except Exception as e:
            print e
            return json.dumps(e)

    def get_contacts_props(self):
        try:
            self.open_DB()
            sql = "PRAGMA table_info(contacts);"

            res = self.current.execute(sql)
            # result = json.dumps(res.fetchall())
            result = res.fetchall()
            print result
            return result
        except Exception as e:
            print e
            return str(e)

    def update_phone(self, first_name, last_name, phone):
        try:
            self.open_DB()
            sql = "UPDATE owners \
                 SET phone = " + str(phone) + " \
                 where owners.first_name = " + first_name + " AND owners.last_name = " + last_name + ";"

            res = self.current.execute(sql)
            result = json.dumps(res.fetchall())
            print "updated phone " + result
            return "updated phone to " + str(4)
        except Exception as e:
            print e
            return str(e)

    def insert_new_contact(self, owner_first_name, owner_last_name, first_Name, last_Name, address, phone, email):
        try:
            self.open_DB()
            sql = "SELECT id FROM owners where first_name = " + owner_first_name + " AND last_name = " + owner_last_name + ";"

            res = self.current.execute(sql)
            owners_id = res.fetchall()[0]
            sql = "INSERT INTO contacts (owners_id, first_Name, last_Name, address, phone, email)"
            sql += " VALUES(" + owners_id + "," + first_Name + "," + last_Name + "," + address + "," + phone + "," + email + ")"
            res = self.current.execute(sql)
            ret = self.current.execute(sql)
            self.commit()
            result = json.dumps(ret.fetchall())
            print result
            return "success"
        except Exception as e:
            print e
            return str(e)

    def count_last_name(self, last_name):
        try:
            self.open_DB()
            sql = "SELECT COUNT(last_name), last_name FROM contacts GROUP BY last_name HAVING last_name LIKE %" + str(
                last_name) + "% ORDER BY last_name;"
            res = self.current.execute(sql)
            # result = json.dumps(res.fetchall())
            result = res.fetchall()
            print result
            return result
        except Exception as e:
            print e
            return str(e)

    def last_owner(self):
        try:
            self.open_DB()
            sql = "SELECT first_name,last_name,max(id) FROM owners"
            res = self.current.execute(sql)
            result = res.fetchall()
            self.commit()
            # result = json.dumps(res.fetchall())

            print result
            return result
        except Exception as e:
            print e
            return str(e)

    def remove_owner(self, owner_first_name, owner_last_name):
        try:
            self.open_DB()
            sql = "DELETE FROM contacts \
                WHERE owner_id IN \
                  ( SELECT owners.id \
                    FROM owners \
                      INNER JOIN contacts on (ccontacts.owner_id = owners.id) \
                    WHERE owners.first_name ='" + owner_first_name + "' AND owners.last_name = '" + owner_last_name + ");"
            res = self.current.execute(sql)
            sql = "DELETE FROM owners where first_name =" + owner_first_name + " AND last_name = " + owner_last_name + ");"
            res = self.current.execute(sql)
            self.commit()
            # result = json.dumps(res.fetchall())
            result = res.fetchall()
            print result
            return "success"
        except Exception as e:
            print e
            return str(e)

    def remove_contact(self, owner_first_name, owner_last_name, first_name, last_name, address, phone, email):
        try:
            self.open_DB()
            sql = "SELECT id FROM owners where first_name = " + owner_first_name + " AND last_name = " + owner_last_name + ";"

            res = self.current.execute(sql)
            owner_id = res.fetchall()[0]
            print "owner id " + str(id[0])

            sql = "DELETE FROM cars \
                where (owner_id = " + str(
                owner_id) + " first_name =" + first_name + ",last_name =" + last_name + ",address = " + address + ",phone = " + str(
                phone) + ",email = " + str(email) + ")"

            res = self.current.execute(sql)
            self.commit()
            result = json.dumps(res.fetchall())
            print result
            return "success"
        except Exception as e:
            print e
            return str(e)
'''

"""
def main_test():
    user1= User("Yos","12345","yossi","zahav","kefar saba","123123123","1111",1,'11')

    db= UserAccountORM()
    db.delete_user(user1.user_name)
    users= db.get_users()
    for u in users :
        print u

if __name__ == "__main__":
    main_test()"""
