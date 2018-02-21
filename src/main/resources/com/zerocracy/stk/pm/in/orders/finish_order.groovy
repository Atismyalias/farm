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
package com.zerocracy.stk.pm.in.orders

import com.jcabi.github.Comment
import com.jcabi.github.Issue
import com.jcabi.xml.XML
import com.zerocracy.Farm
import com.zerocracy.Par
import com.zerocracy.Project
import com.zerocracy.entry.ExtGithub
import com.zerocracy.farm.Assume
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Boosts
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.in.Orders
import com.zerocracy.pm.staff.Roles
import com.zerocracy.radars.github.Job
import org.cactoos.collection.Mapped
import org.cactoos.func.FuncOf
import org.cactoos.list.ListOf
import org.cactoos.scalar.Or

import java.util.concurrent.TimeUnit

def exec(Project project, XML xml) {
    new Assume(project, xml).notPmo()
    new Assume(project, xml).type('Finish order')
    ClaimIn claim = new ClaimIn(xml)
    String job = claim.param('job')
    Orders orders = new Orders(project).bootstrap()
    long minutes = (System.currentTimeMillis() - orders.startTime(job).time) / TimeUnit.MINUTES.toMillis(1L)
    String login = orders.performer(job)
    Estimates estimates = new Estimates(project).bootstrap()

    String quality
    if (claim.params().containsKey('quality')) {
        quality = claim.params()['quality']
    } else {
        quality = issueQuality(project, job)
    }
    if (quality == 'good' || quality == 'acceptable') {
        def extra = quality == 'good' ? 5 : 0
        ClaimOut out = claim.copy()
            .type('Make payment')
            .param('cause', claim.cid())
            .param('login', login)
            .param('reason', 'Order was successfully finished')
            .param('minutes', new Boosts(project).bootstrap().factor(job) * 15 + extra)
        if (estimates.exists(job)) {
            out = out.param('cash', estimates.get(job))
        }
        out.postTo(project)
    } else {
        new ClaimOut()
            .type('Notify job')
            .param('job', job)
            .param('message', new Par('Quality is low, no payment, see §31').say())
            .postTo(project)
    }
    orders.resign(job)
    new ClaimOut()
        .type('Order was finished')
        .param('cause', claim.cid())
        .param('job', job)
        .param('login', login)
        .param('minutes', minutes)
        .postTo(project)
}

def issueQuality(Project project, String job) {
    Farm farm = binding.variables.farm
    Roles roles = new Roles(project).bootstrap()
    Orders orders = new Orders(project).bootstrap()
    List<String> arcs = roles.findByRole('ARC')
    String performer = orders.performer(job)
    Issue.Smart issue = new Issue.Smart(new Job.Issue(new ExtGithub(farm).value(), job))
    if (issue.pull) {
        List<String> authors = new ListOf<>(
            new Mapped<>(
                { Comment cmt -> new Comment.Smart(cmt).author().login() },
                issue.comments().iterate(issue.createdAt())
            )
        )
        boolean hasArc = new Or(
            new FuncOf<String, Boolean>({ String author -> arcs.contains(authors) }),
            authors
        ).value()
        if (hasArc && authors.contains(performer)) {
            return 'acceptable'
        }
        return 'bad'
    }
    'acceptable'
}