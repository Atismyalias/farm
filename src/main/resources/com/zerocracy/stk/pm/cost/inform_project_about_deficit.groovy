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
import com.zerocracy.jstk.Project
import com.zerocracy.jstk.cash.Cash
import com.zerocracy.pm.ClaimOut
import com.zerocracy.pm.cost.Estimates
import com.zerocracy.pm.cost.Ledger

def exec(Project project, XML xml) {
  new Assume(project, xml).notPmo()
  new Assume(project, xml).type('Ping')
  Ledger ledger = new Ledger(project).bootstrap()
  Cash cash = ledger.cash()
  Cash locked = new Estimates(project).bootstrap().total()
  if (ledger.deficit() && cash > locked) {
    ledger.deficit(false)
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        "The project is properly funded, cash balance is ${cash} (${locked} in active orders)," +
        ' we will continue to assign jobs to performers.'
      )
      .postTo(project)
  }
  if (!ledger.deficit() && cash < locked) {
    ledger.deficit(true)
    new ClaimOut()
      .type('Notify project')
      .param(
        'message',
        "The project is out of funds, cash balance is ${cash} (${locked} in active orders)," +
        ' we temporarily stop assigning jobs. Please, fund the project ASAP.'
      )
      .postTo(project)
  }
}
