/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.Farm;
import com.zerocracy.pm.ClaimOut;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Milestone rebound.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbMilestone implements Rebound {
    /**
     * Milestone json key.
     */
    private static final String JSON_KEY = "milestone";

    @Override
    public String react(
        final Farm farm,
        final Github github,
        final JsonObject event
    ) throws IOException {
        final String answer;
        if (event.containsKey(RbMilestone.JSON_KEY)) {
            final JsonObject milestone = event.getJsonObject(
                RbMilestone.JSON_KEY
            );
            new ClaimOut()
                .type("Add milestone")
                .param("milestone", milestone.getString("title"))
                .param("date", milestone.getString("due_on"));
            answer = String.format(
                "Milestone submitted: %d",
                milestone.getInt("number")
            );
        } else {
            answer = "Not a milestone event";
        }
        return answer;
    }
}
