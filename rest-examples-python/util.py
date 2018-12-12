#
# Copyright 2017 Apereo
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import time
import urllib
import binascii
import hashlib


def urlEncode(text):
    return urllib.urlencode({"q": text})[2:]


# Generate an equella token for logging in
def generateToken(username, sharedSecretId, sharedSecretValue):
    seed = str(int(time.time())) + "000"
    id2 = urlEncode(sharedSecretId)
    if not (sharedSecretId == ""):
        id2 += ":"

    return "%s:%s%s:%s" % (
        urlEncode(username),
        id2,
        seed,
        binascii.b2a_base64(
            hashlib.md5(username + sharedSecretId + seed + sharedSecretValue).digest()
        ),
    )
