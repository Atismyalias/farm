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
package com.zerocracy.stk.pm.cost

import com.jcabi.xml.XML
import com.zerocracy.farm.Assume
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimIn
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Ledger
import com.zerocracy.pmo.Catalog

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Order was finished')
  ClaimIn claim = new ClaimIn(xml)
  String job = claim.param('job')
  String login = claim.param('login')
  Farm farm = binding.variables.farm
  Cash fee = new Catalog(farm).bootstrap().fee(project.pid())
  if (fee != Cash.ZERO) {
    new Ledger(project).bootstrap().add(
      new Ledger.Transaction(
        fee,
        'expenses', 'fee',
        'liabilities', 'zerocracy',
        "Zerocracy fee for ${job} completed by @${login}"
      ),
      new Ledger.Transaction(
        fee,
        'liabilities', 'zerocracy',
        'assets', 'cash',
        "Zerocracy fee paid in cash for ${job}"
      )
    )
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        "Management fee ${fee} has been deducted," +
        ' see [§23](http://datum.zerocracy.com/pages/policy.html#23)'
      )
      .postTo(project)
  }
}
