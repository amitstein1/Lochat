import socket
import threading
from sys import argv
# import time
import SQL_ORM
import classes
import geopy.distance

EWOULDBLOCK = 10035
EINPROGRESS = 10036
EALREADY = 10037
Eclient_sockRESET = 10054
ENOTclient_sock = 10057
ESHUTDOWN = 10058
WSAEclient_sockABORTED = 10053

change_total_clients = threading.Lock()

users = {}  # global
users_lock = threading.Lock()

groups = {}  # global
groups_lock = threading.Lock()

open_group_id_lock = threading.Lock()
open_group_id = None

open_message_id_lock = threading.Lock()
open_message_id = None

db = SQL_ORM.MyDataBaseORM()
db_lock = threading.Lock()

SIZE_HEADER_FORMAT = "000000|"
size_header_size = len(SIZE_HEADER_FORMAT)
DEBUG = True


def recv_by_size(sock):
    str_size = ""
    data_len = 0
    # getting the length of the data : SIZE_HEADER_FORMAT - first 7 chars
    while len(str_size) < size_header_size:
        str_size += sock.recv(size_header_size - len(str_size))
        if str_size == "":
            break
    data = ""
    if str_size != "":
        data_len = int(str_size[:size_header_size - 1])
        while len(data) < data_len:
            data += sock.recv(data_len - len(data))
            if data == "":
                break
    if DEBUG and str_size != "" and len(data) < 100:
        print "\nReceived(%s)<<<%s" % (str_size, data)
    if data_len != len(data):
        data = ""
    return data


def send_with_size(sock, data):
    data = str(len(data)).zfill(size_header_size - 1) + "|" + data

    sock.send(data)

    if DEBUG and len(data) < 100:
        print "\nSent>>>" + data


class State_enum:
    start = 1
    after_hint = 2
    solved = 3


def get_dist_between_two_locs(loc1, loc2):
    coords_1 = (loc1.latitude, loc1.longitude)
    coords_2 = (loc2.latitude, loc2.longitude)
    return geopy.distance.vincenty(coords_1, coords_2).km


def get_user_groups_by_loc_and_put_user_in_groups_and_update_manager_dynamic_groups(location, username):
    global groups
    global users
    global db
    global db_lock
    user_groups = {}
    hidden_groups = {}
    hidden_groups_in_loc = {}
    manage_groups = {}
    manage_groups__in_loc = {}
    list_usergroup_table = db.get_all_user_groups(username)#db.get_all_usergroups
    print "list_usergroup_table:" + str(list_usergroup_table)
    dict_usergroup_table_by_group_id = {}
    for tuple_user_group in list_usergroup_table:
        dict_usergroup_table_by_group_id[str(tuple_user_group[2])]=tuple_user_group

    for group in groups.values():
        print "------------------------------------"
        distance = get_dist_between_two_locs(location, group.mid_loc)
        print "groupe name=" + group.name
        print "user loc=" + str(location)
        print "group loc==" + str(group.mid_loc)
        print "group radius=" + str(group.radius)
        print "distance=" + str(distance)
        s = ''
        # --------------------------------------------
        # if in the future i will want to show all of the hidden and manage groups and just
        # don't let the user to insert to the groups if he is not in the location of the group
        if group.manager_user_id == username:
            # changing all manager dynamic groups in global groups
            if group.type == classes.group_type_Enum.dynamic:
                group.mid_loc = users[username].location

            manage_groups[group.id] = group
            s += 'manager, '
        if group.id in users[username].hidden_groups.keys():  # the hidden groups of the last updated dictionary
            hidden_groups[group.id] = group
            s += 'hidden'
        # --------------------------------------------
        if distance <= group.radius:
            print "user is in group location :) lets add him to the group if he is not in yet"
            # i can also do straight: groups[group.id].users[username] = users[username]
            # and if the user is already in the group it will override his last values with the current.
            if username not in group.users.keys():
                groups_lock.acquire()
                # we must put otheruser object because unless we will have recurtion while trying to get to the groups of the user.
                # because in the groups there is var of users in the group and to each user we go to user object again and again
                # groups and again users of groups and again user ............

                # say no to recurtion!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
                user_other_user = classes.OtherUser(username, users[username].md5_pw, users[username].location,
                                                    users[username].fname, users[username].lname,
                                                    users[username].address, users[username].gender,
                                                    users[username].phone_num, users[username].email,
                                                    users[username].pic_data,
                                                    users[username].is_connected, users[username].l_seen)
                groups[group.id].users[username] = user_other_user
                groups_lock.release()

            # now we decide in which list will the group go to - get help by the function in the SQL_ORM
            if group.manager_user_id == username:
                manage_groups__in_loc[group.id] = group
                s += 'manager loc, '

            if group.id in users[username].hidden_groups.keys():  # the hidden groups of the last updated dictionary
                hidden_groups_in_loc[group.id] = group
                s += 'hidden loc'
            else:
                user_groups[group.id] = group
                s += 'user_group'

            #in any case - if the group not in usergroup we need to add
            #  the group so next time the user will be in this place we
            #  know if the group is hidden or not because now we have the
            #  posibility to change the user group relationship to hidden
            #  and know about that because we never delete from usergroup table
            if group.id not in dict_usergroup_table_by_group_id.keys():
                #lets make the group related to the user
                db.add_user_group(username,group.id,is_hidden=False,is_manager=False)
        else:
            print "user not in group location!!!"

        print s

    return user_groups, hidden_groups, manage_groups
    # return user_groups, hidden_groups, hidden_groups_in_loc, manage_groups, manage_groups__in_loc


def get_location_obj_by_str(location_str):
    loc_list = location_str.split("|")
    latitude = loc_list[0]
    longitude = loc_list[1]
    new_loc = classes.Location(latitude, longitude)
    return new_loc


def autenticate_client(client_sock, tid):
    """
    :param client_sock:
    :param tid:
    :return: username]
    the method will recv data from the client: function: (sign in/log in) and then the related data.
    them the function check if there is client like this - in global dictionaries. if there is,
    the function will send the client the data and tell him to start the application
    if there is no client like this the client is supposed to send all of the relevant data - a log in sent.
    """
    global users
    done_autenticate = False
    client_sock.settimeout(None)  # recv in block mode - we will not continue before the client's autentication
    username_ = ""
    client_exit = False

    try:
        while not done_autenticate:
            data = client_sock.recv(100000)
            # data = recv_by_size(client_sock)
            if data == "":
                print "client disconnected"
                client_exit = True;
                break
            print 'received<<< ' + data
            l_data = data.split('%')
            action = l_data[0]
            params = l_data[1].split('~')
            print "action=" + action
            print "params=" + str(params)

            if action == 'client_exit':
                # need to close connection
                client_exit = True
                done_autenticate = True
            elif action == "sign_in":
                username, md5_pw = params
                try:
                    if users[username].md5_pw == md5_pw:
                        done_autenticate = True
                        print "there is such user"
                        to_send = "signed_in_successfully" + "%" + str(users[username])
                        username_ = users[username].username
                    else:
                        print "password is wrong"
                        to_send = "signed_in_unsuccessfully%wrong_password"
                except:
                    print "there is no such user"
                    to_send = "signed_in_unsuccessfully%no_such_user"


            elif action == "register":
                [username, md5_pw, location_str, fname, lname, address, phone_num, email, gender, pic_data] = params
                print 222222222222222222
                if not username in users.keys():
                    print 111111111111111
                    l_seen = "on"  # will be used just for other clients in the next edition
                    is_connected = True
                    location_obj = get_location_obj_by_str(location_str)
                    user_groups, hidden_groups, manage_groups = {}, {}, {}
                    friends = {}
                    users_who_asked_request = {}
                    users_who_will_answer_request = {}
                    new_user = classes.User(username, md5_pw, location_obj, fname, lname, address, gender, phone_num,
                                            email, pic_data, is_connected, l_seen, user_groups,
                                            hidden_groups, manage_groups, friends, users_who_asked_request,
                                            users_who_will_answer_request)
                    users_lock.acquire()
                    users[username] = new_user
                    print "servers data updated"
                    users_lock.release()

                    db_lock.acquire()
                    db.insert_new_user_to_users(new_user)
                    #db.insert_user_to_his_groups(new_user.username, new_user.user_groups)
                    #print "data base updated"
                    db_lock.release()

                    print "new user added to the app"
                    to_send = "registered_successfully"  # + "%" + str(new_user)
                else:
                    print "username is already taken"
                    to_send = 'registered_unsuccessfully_username_taken'

            else:
                print "error - got wrong data"
                to_send = "error%got_wrong_data"

            if not client_exit:
                to_send = to_send + "\n"  # str(tid) + '%' + to_send + '\n'
                print "sent>>>" + to_send
                client_sock.send(to_send)
                # send_with_size(client_sock,to_send)
    except Exception as err:
        print "General Error:", err.message
        return None

    return username_, client_exit  # so the thread will know who is his client and also know what the client know


def delete_user_from_groups_that_not_in_them_any_more(username, last_user_groups, last_hidden_groups,
                                                      last_manage_groups, now_user_groups, now_hidden_groups,
                                                      now_manage_groups):
    global groups
    global groups_lock
    for group_id in now_user_groups.keys():
        if group_id not in last_user_groups.keys():
            groups_lock.acquire()
            del groups[group_id].users[username]  # delete the user from the
            groups_lock.release()

    for group_id in now_hidden_groups.keys():
        if group_id not in last_hidden_groups.keys():
            groups_lock.acquire()
            del groups[group_id].users[username]  # delete the user from the
            groups_lock.release()

    for group_id in now_manage_groups.keys():
        if group_id not in last_manage_groups.keys():
            groups_lock.acquire()
            del groups[group_id].users[username]  # delete the user from the
            groups_lock.release()


class HandleClientThread(threading.Thread):
    def __init__(self, ip, port, client_sock, tid):
        """
            the thread that will handle the clien while the main accepts to clients


            at the beginning the server will try to autenticate the client to the system
            at first the server soes not know anything about his client.
            after the client sent to the server "sign_in username password" the server will check if the client is in the users dictionary
        """
        threading.Thread.__init__(self)
        # print "New thread started for "+ip+":"+str(port)
        self.ip = ip
        self.port = port
        self.client_sock = client_sock
        self.tid = tid  # another option  : threading.current_thread().ident

    def are_dicts_equal(self, d1, d2):
        if len(d1) != len(d2):
            return False
        for key in d1.keys():
            try:
                if d1[key] != d2[key]:
                    return False
            except:
                print "the key " + str(key) + "int d1 does not exist in d2"
                return False
        return True

    def run(self):
        """
        the call thread_obj.start() will auto call to this method !!!!
        """
        global total_clients
        global father_going_to_close
        global open_group_id
        global open_message_id

        # global open_user
        # global open_group
        # global open_msg_id

        change_total_clients.acquire()
        total_clients += 1
        change_total_clients.release()

        # user_in_client = autenticate_client(self.client_sock, self.tid)
        # username = user_in_client.username
        # print 'after autenticate_client'

        # self.client_sock.setblocking(1)
        self.client_sock.settimeout(10)  # Every some second will check whether Main want to kill
        print "New Thread after Accept new client_socket connection from : " + self.ip + ":" + str(self.port)

        # each server thread have this variable and before the send
        # the it is his responsibility to change the value of the key - the property
        dict_props_to_send = {"user_obj": None, "username": None, "md5_pw": None, "location": None, "fname": None,
                              "lname": None, "address": None, "gender": None, "phone_num": None, "email": None,
                              "pic_data": None, "is_connected": None, "l_seen": None, "user_groups": None,
                              "hidden_groups": None, "manage_groups": None}

        # last_user_obj = users[username]
        print "before main app while"

        # if the client does not share his location he can not be in any group - so we will not update his groups here - if we will he would be able to be in this group also if he does not apear in the group location
        # users[username].user_groups = get_user_groups_by_loc_and_put_user_in_groups_and_update_manager_dynamic_groups(users[username].location) #for the case when the client does not share his location - we will
        # log_in = True
        # client_exit = False;
        while True:
            log_in = True
            print 'before autenticate_client'
            username, client_exit = autenticate_client(self.client_sock, self.tid)
            print 'after autenticate_client'
            last_user_groups, last_hidden_groups, last_manage_groups = {}, {}, {}
            first_run_while = True
            while log_in and not client_exit:
                try:
                    print "before recv"
                    data = self.client_sock.recv(100000)  # every 5 seconds we will het the location!
                    # data = recv_by_size(self.client_sock)
                    print "after recv"
                    if data == "":
                        print "Got empty data from Recv Will close this client socket"
                        client_exit = True
                        break
                    print str(self.tid) + ": Received<<< " + data

                    # now acording to the data and other client changes just update this user and send it
                    l = data.split('%')
                    action = l[0]
                    params = l[1:]
                    if action == 'log_out':
                        # need to do autentication again
                        log_in = False
                        break
                    if action == 'client_exit':
                        # need to close connection
                        client_exit = True
                        break

                    if action == 'new_loc':
                        location_str = params[0].split("new_loc")[0]
                        new_location_obj = get_location_obj_by_str(location_str)
                        users[username].location = new_location_obj  # change location

                        # now we will update the location of the usser in his manager groups
                        # update_manager_location_in_manage_groups(username)#update the global groups!!
                        # because the manager groups in the user are

                        # user_groups, hidden_groups, hidden_groups_in_loc, manage_groups, manage_groups__in_loc
                        users[username].user_groups, users[username].hidden_groups, users[
                            username].manage_groups = \
                            get_user_groups_by_loc_and_put_user_in_groups_and_update_manager_dynamic_groups(
                                new_location_obj, username)  # change user groups by the new location

                        # now delete this user from the groups that he is not in them any more
                        #only in the first run because we do bot know nothing about the groups after
                        if first_run_while:
                            first_run_while = False
                            last_user_groups, last_hidden_groups, last_manage_groups = users[username].user_groups, \
                                                                                       users[
                                                                                           username].hidden_groups, \
                                                                                       users[username].manage_groups
                        #delete user from the global groups that he is't in them
                        delete_user_from_groups_that_not_in_them_any_more(username, last_user_groups,
                                                                          last_hidden_groups, last_manage_groups,
                                                                         users[username].user_groups,
                                                                          users[username].hidden_groups,
                                                                          users[username].manage_groups)
                        last_user_groups, last_hidden_groups, last_manage_groups = users[username].user_groups, users[
                            username].hidden_groups, users[username].manage_groups
                        # change_loc_of_manager_groups(users[username])#input -user object
                    elif action == "answer_to_other_client_request":
                        this_user_id = username
                        other_user_id = params[0]
                        request_answer = int(params[1])
                        users_lock.acquire()
                        del users[other_user_id].users_who_will_answer_request[
                            this_user_id]  # in any case because this user had answered - friends or not.
                        if request_answer == classes.RequestAnswer.accept:
                            # users[other_user_id].friend[this_user_id] = self
                            pass
                            # because this user answering to other client request we need to update also his groups: all the groups of requests.
                            # so he do not need to check every iteration if his request was answerd ot something
                            # also update database
                            # add this OtherUser obj to his list and his OtherUser obj to my list

                        users_lock.release()
                    elif action == "request_to_other_client":
                        pass
                    elif action == "new_md5_pw":
                        pass
                    elif action == "new_message":
                        # now we will update the user groups and manager groups
                        print "params=" + str(params[0])
                        [msg_type, group_id, src_user_id, dst_id, send_time, data] = params[0].split('&')

                        msg_id = open_message_id
                        open_message_id = str(int(open_message_id) + 1)
                        db.set_open_message_id(open_message_id)


                        new_message = classes.Message(msg_id,msg_type, group_id, src_user_id, dst_id, send_time, data)
                        print "new message!!!=" + str(new_message)
                        groups[group_id].messages.append(new_message)
                        print "message was added to the group in global groups"
                        #update the data in the group
                        users[username].user_groups, users[username].hidden_groups, users[
                            username].manage_groups = \
                            get_user_groups_by_loc_and_put_user_in_groups_and_update_manager_dynamic_groups(
                                users[username].location, username)  # change user groups by the new location

                        # update the data in the group in db!
                        db.add_new_message(new_message)


                    elif action == "new_manager_group":
                        # now we will update the user groups and manager groups
                        group_type, group_loc_str, radius_str, group_name, picData, managerId, time_created = params[
                            0].split('&')
                        group_loc_obj = get_location_obj_by_str(group_loc_str)
                        ##because of problems in the sending we also know that the groupe location in the group creation is the manager location
                        group_loc_obj = classes.Location(users[username].location.latitude,
                                                         users[username].location.longitude)
                        radius = float(radius_str)
                        group_id = open_group_id
                        open_group_id = str(int(open_group_id) + 1)
                        db.set_open_group_id(open_group_id)
                        new_group = classes.Group(group_id, group_type, group_loc_obj, radius, group_name, picData,
                                                  managerId, {}, [], time_created)
                        print "new group!!!=" + str(new_group)
                        groups[new_group.id] = new_group
                        db.add_new_group(new_group)
                        is_hidden = False  # false
                        is_manager = True  # true
                        db.add_user_group(username, group_id, is_hidden,
                                          is_manager)  # so the server will know to add his group to the right (manage and hidden) dict only when up - when importing data.
                        users[username].user_groups[new_group.id] = new_group
                        users[username].manage_groups[new_group.id] = new_group

                        # the user must be in this group  right now
                        # we do not need to update the users in the group because they change every few seconds.
                        print "new group:" + str(new_group)
                        print "groups:" + str(groups)
                        # here send to the client if the group is llegal -radius too big fo example
                    elif action == "hide_user_group":
                        print "before hide_group"
                        group_id = params[0]
                        print "user groups before del" + str(users[username].user_groups)
                        del users[username].user_groups[group_id]  # remove group from user groups
                        print "user groups after del" + str(users[username].user_groups)
                        print "after delete group id:" + group_id + "from user groups"
                        users[username].hidden_groups[group_id] = groups[group_id]  # add group to hidden groups
                        print "after add group id:" + group_id + "to hidden groups"
                        db.hide_user_group(username, group_id)  # with user_group
                        print "after database update: group id:" + group_id + "is now hidden"
                    elif action == "unhide_user_group":
                        print "before unhide_group"
                        group_id = params[0]
                        print "hidden groups before del" + str(users[username].hidden_groups)
                        del users[username].hidden_groups[group_id]  # remove group from user groups
                        print "user groups after del" + str(users[username].user_groups)
                        print "after delete group id:" + group_id + "from hidden groups"
                        users[username].user_groups[group_id] = groups[group_id]  # add group to hidden groups
                        print "after add group id:" + group_id + "to user_groups groups"
                        db.unhide_user_group(username, group_id)  # with user_group
                        print "after database update: group id:" + group_id + "is not hidden anymore - see in user_groups"
                    elif action == "give_mangement_of_group":
                        # there is only one manager per group - for now
                        # params are to who and which group
                        pass

                        # the client sends only what he want to change and the server sends the whole user after an update acording the change
                    to_send_user = "user_obj%" + str(users[username]) + '\n'
                    self.client_sock.send(to_send_user)
                    # send_with_size(self.client_sock, to_send_user)
                    print "sent>>>" + to_send_user
                    # time.sleep(5)#the server also do not need to send his client the data every single time

                    """
                    #now we are changeing the cuurent user - the things that can be changed by other clients are: request_by_other_client_list,  is the list
    
                    empty_user= classes.User('-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-','-')
                    user_now = users[username] #after changes by other clients
                    if self.are_dicts_equal(!user_now.users_who_will_answer_request,user_in_client):
                        get
    
                    if action == 'new_loc':
                        location_str = params[0]
                        new_location_obj = get_location_obj_by_str(location_str)
                        users[username].location=new_location_obj #change location
                        users[username].user_groups = get_user_groups_by_loc_and_put_user_in_groups_and_update_manager_dynamic_groups(new_location_obj, username) #change user groups by the location
                    elif action == "request_other_client":
                        pass
                    elif action == "answer_to_other_client_request":
                        other_user_id = params[0]
                        this_user_id = username
                        request_answer = int(params[1])
                        if request_answer == classes.RequestAnswer.ignore:
                            users_lock.acquire()
                            del users[other_user_id].users_who_will_answer_request[this_user_id]
    
                            users_lock.release()
    
                    elif action == "new_md5_pw":
                        pass
                    elif action == "new_message":
                        pass
                    elif action == "new_hidden_group":
                        pass
                    elif action == "new_manager_group":
                        pass
                    user_obj_after_mini_updata = users[username]
                    #continue doing it to all of the values that the client can change - not lists
                    properties_names_list = self.update_user_and_get_changed_props(user_obj_before_mini_updata,user_obj_after_mini_updata,dict_props_to_send)#need to be on the way to save running time because we alreeady checking
    
                    #dictionary of property name in string and the property - we update the properit in the dictionary before send
                    for prop in properties_names_list:
                        to_send = prop + "~" + str(dict_props_to_send[prop])+ "\n"
                        #or try to add all the to_send data in one send where there is a split character that
                        #did not used like: @^=+<>?~`;:%* (-/\ and space is mybe for time and date so we will not use them)
                        print "before send"
                        self.client_sock.send(to_send)
                        print str(self.tid) + ": Sent   >>>" + to_send
                    print "-----------------------------------------------
                    self.
                    to_sent_user = self.get_with_just_data_the_client_dont_know()
                    user_in_client = users[username] #this is the user that the client know about
                    self.client_sock.send(to_sent_user)
    
                    #time.sleep(5)#the server also do not need to send his client the data every single time
                    """


                except socket.error as e:

                    if e.errno == Eclient_sockRESET:  # 'new_clientection reset by peer'
                        print "Error %s - Seems Client Disconnected. try Accept new Client " % e.errno
                        break
                    elif e.errno == EWOULDBLOCK or str(e) == "timed out":  # (time out) Client didnt send any data yet
                        if father_going_to_close:
                            print "Father Going To Die"
                            self.client_sock.close()
                            break
                        else:
                            print "did not get a new location but eather way we will send the cliet data that have been changed by other client"
                            to_send_user = "user_obj%" + str(users[username]) + '\n'
                            self.client_sock.send(to_send_user)
                            # send_with_size(self.client_sock,to_send_user)
                            print "sent>>>" + to_send_user
                        print ".",
                        continue
                    else:
                        print "Unhandled Socket error at recv. Server will exit %s " % e
                        break

            if (client_exit):
                break  # finish communication with this client
            # else continue - user just logged out

        print "Client disconnected..."
        print "Before close son socket - total clients = %d (%d)" % (how_many_clients("Child"), total_clients)
        change_total_clients.acquire()
        total_clients -= 1
        change_total_clients.release()

        self.client_sock.close()

    '''
        def update_user_and_get_changed_props(self,user_in_client,user_after_changes_of_other_uers,dict_props_to_update_by_client):
            """
            Business Logic - here write what the real work of this thread
            """
            global total_clients
            global users
            global users_lock
            global groups
            global groups_lock

            properties_names_list = []
            #the only thing that we do not need to send to the client is his location because he gets it first
            #from the google server and he sent it to this server
            user_changes_to_Sent= classes.User("",""null null drgrg)
            if prop == "location":
            user_groups_in_client = user_in_client.user_groups
            user_groups_updated = get_groups_by_loc(dict_props_to_update_by_client[prop])#problem in this method
            if before_user_groups != after_user_groups:
                properties_names_list.append("user_groups")
                dict_props_to_send["user_groups"] = after_user_groups
            if before_hidden_groups != after_hidden_groups:
                properties_names_list.append("hidden_groups")
                dict_props_to_send["hidden_groups"] = after_hidden_groups
            if before_user_groups != after_manage_groups:
                properties_names_list.append("manage_groups")
                dict_props_to_send["manage_groups"] = after_manage_groups

            if last_user_obj.location !=new_location_obj:
                properties_names_list.append("location")
                dict_props_to_send["location"] = new_location_obj
                print "location of " + last_user_obj.username + " was updated to:" + str(new_location_obj)
                #check for changes in all of the propertie + all of the lists
            else:
                print "nothing have changed in user object of:" + last_user_obj.username


            return properties_names_list
    '''


def how_many_clients(caller):
    if caller == "main":
        return threading.activeCount() - 1
    else:
        return threading.activeCount() - 1


def import_all_from_data_base():
    global users
    global groups
    global db
    global open_group_id
    global open_message_id
    # do not need to lock any variable because there is no thread yet
    # all the users here are with empty user_group dict and with the wrong location
    # this will change of course immediatly by the son thread servers
    users = db.get_all_users()

    print "users:---------------------------------------------\n" + str(users)
    groups = db.get_all_groups()
    print "groups:---------------------------------------------\n" + str(groups)

    open_group_id = db.get_open_group_id()
    print "next open_group_id=" + str(open_group_id)

    open_message_id = db.get_open_message_id()
    print "next open_message_id=" + open_message_id


MAX_NUM_OF_CLIENTS = 5


def main(server_port):  # the same port in the client
    global total_clients
    global father_going_to_close
    global groups
    global users

    father_going_to_close = False

    total_clients = 0

    import_all_from_data_base()

    server_sock = socket.socket()
    server_ip = "0.0.0.0"  # socket.gethostname()

    #  SO_REUSEADDR means : reopen  socket even if its in wait state from last execution without waiting
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_sock.bind((server_ip, server_port))
    print "After bind to ip: " + server_ip + ", port: " + str(server_port)

    server_sock.listen(MAX_NUM_OF_CLIENTS)
    print "After listen"

    threads = []
    tid = 0
    time_out = 10  # miliseconds
    server_sock.settimeout(10)
    # print "time out is set to " + str(time_out) + " miliseconds"
    cnt = 0

    while True:
        try:
            (client_sock, (ip, port)) = server_sock.accept()
            print "after accept"
            print "\n new client\n"
            tid += 1
            # create the thread
            # the thread will handle the client from now
            # the thread will check if the client is signed in and also the thread will create a new user if the user is new
            new_thread = HandleClientThread(ip, port, client_sock, tid)  # this line call to __init__ of new_thread
            new_thread.start()  # this call to run method of new_thread

            print "Clients = %d (%d) " % (how_many_clients("main"), total_clients)
            threads.append(new_thread)


        except socket.timeout:
            cnt += 1
            #print users
            #print groups
            print "#\n" if cnt % 10 == 0 else ',',  # to show that the server is always up

        except KeyboardInterrupt:
            print "\nGot ^C Main\n"
            father_going_to_close = True

    server_sock.close()
    print "Server Says: Bye Bye ...."
    for t in threads:
        t.join()
    return


# End main

global father_going_to_close
if __name__ == '__main__':

    try:
        # if len(argv) != 2:
        #   print "Usage: <port_number>"
        #    exit()
        # else:
        #    port = int(argv[1])
        #    main(port)
        school_port = 20139
        home_port = 4900
        main(home_port)
    except KeyboardInterrupt:
        print "\nGot ^C Main\n"
        father_going_to_close = True
