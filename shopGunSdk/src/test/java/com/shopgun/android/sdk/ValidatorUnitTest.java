/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk;


import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.Validator;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class ValidatorUnitTest {

    public static final String TAG = Constants.getTag(ValidatorUnitTest.class);

    @Test
    public void testIsEmailVaild() throws Exception {

        String[] inValid = new String[]{null, "", "@", "@@", "d@@h", "d@k@h", "@hvam", " @ ", "null", "danny@", " d@h.dk ", "d@h.dk ", " d@h.dk"};
        for (String s : inValid) {
            Assert.assertFalse(Validator.isEmailValid(s));
        }

        String[] valid = new String[]{"danny@hvam.dk", "d@h", "d @ h", "d@h.dk", "4321@fdsafd", "[]{}()@[]{}()", "∂∑˙∆´∫∑@˙¨ˆ∂∑∆"};
        for (String s : valid) {
            Assert.assertTrue(Validator.isEmailValid(s));
        }

    }

    @Test
    public void testValidVersion() throws Exception {

        String[] valid = {"2.0.0", "2.0.0-rc.2", "2.0.0-rc.1", "1.0.0", "1.0.0-beta",
                "1.0.0-b", "1.0.0-beta", "1.0.0-chocolate", "1.0.0-15948", "1.0.0-rc-1"};


        // Out regex haven't covered the case "1.0.0-"
        String[] invalid = {null, "jens", "", ".", "v", "v.1", "v1.0", "v1.0.0",
                "1.", "1.0", "1.0.", "1.0.0.0", "v1.0.0-beta.2.2", "v1-beta"};

        for (String s : valid) {
            Assert.assertTrue(Validator.isAppVersionValid(s));
        }

        for (String s : invalid) {
            Assert.assertFalse(Validator.isAppVersionValid(s));
        }

    }

}
