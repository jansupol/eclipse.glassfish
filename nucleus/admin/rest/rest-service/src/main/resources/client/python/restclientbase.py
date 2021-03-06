#
# Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

import traceback
import urllib
import random
import string
import mimetypes
import sys
from restresponse import *
from connection import *

class RestClientBase:
    def __init__(self, connection, parent, name = None):
        self.connection = connection
        self.parent = parent
        self.entityValues = { }
        self.children = {}

        if name:
            self.setName(name)
            self.name = name

        try:
            restResponse = self.connection.get(self.getRestUrl())

            self.status = restResponse.getStatus()
            self.entityValues = restResponse.getEntityValues()
            self.children = restResponse.getChildren()
        except Exception as e:
            print e
            traceback.print_exc(file=sys.stdout)

    def getParent(self):
        return self.parent

    def getRestUrl(self):
        return self.getParent().getRestUrl() + self.getSegment()

    def getSegment(self):
        return ""

    def getStatus(self):
        return self.status

    def getMessage(self):
        return self.message

    def save(self):
        response = self.connection.post(self.getRestUrl(), urllib.urlencode(self.entityValues), headers={'Content-type': 'application/x-www-form-urlencoded'})
        self.status = response.getStatus()

    def delete(self):
        response = self.connection.delete(self.getRestUrl())
        self.status = response.getStatus()

    def execute(self, endPoint, method = "GET", payload = {}, needsMultiPart = False):
        if method == "POST":
            if needsMultiPart:
                content_type, body = self.encode_multipart_formdata(payload)
                restResponse = self.connection.post(self.getRestUrl() + endPoint, body, headers={'Content-type': content_type})
            else:
                restResponse = self.connection.post(self.getRestUrl() + endPoint, urllib.urlencode(payload), headers={'Content-type': 'application/x-www-form-urlencoded'})
        else:
            restResponse = self.connection.request(self.getRestUrl() + endPoint, method, urllib.urlencode(payload))

        if restResponse:
            self.status = restResponse.getStatus()
            return restResponse
        else:
            self.status = -1
            # raise an exception

    def setValue(self, key, value):
        self.entityValues[key] = value

    def getValue(self, key):
        return self.entityValues[key]

    def encode_multipart_formdata(self, args):
        BOUNDARY = '----------' + self._random_string (30)
        EOL = '\r\n'
        body = []
        for (key, value) in args.items():
            if type(value) is file:
                filename = value.name
                body.append('--' + BOUNDARY)
                body.append('Content-Disposition: form-data; name="%s"; filename="%s"' % (key, filename))
                body.append('Content-Type: %s' % self.get_content_type(filename))
                body.append('')
                body.append(value.read())
            else:
                body.append('--' + BOUNDARY)
                body.append('Content-Disposition: form-data; name="%s"' % key)
                body.append('')
                body.append(str(value))

        body.append('--' + BOUNDARY + '--')
        body.append('')
        content_type = 'multipart/form-data; boundary=%s' % BOUNDARY
        return content_type, EOL.join(body)

    def get_content_type(self, filename):
        return mimetypes.guess_type(filename)[0] or 'application/octet-stream'

    def _random_string (self, length):
        return ''.join (random.choice (string.letters) for ii in range (length + 1))
