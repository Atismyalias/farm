/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.bundles.bug_label

import com.jcabi.github.Github
import com.jcabi.github.Repos
import com.jcabi.xml.XML
import com.zerocracy.jstk.Farm
import com.zerocracy.jstk.Project
import com.zerocracy.radars.github.RbOnPullRequest
import javax.json.Json
import org.cactoos.iterable.IterableOf

def exec(Project project, XML xml) {
  Github github = binding.variables.github
  def repo = github.repos().create(new Repos.RepoCreate("test", false))
  def pull = repo.pulls().create("New PR", "master", "master")
  final xpath = String.format(
    "links/link[@rel='github' and @href='%s']",
    repo.coordinates().toString().toLowerCase(Locale.ENGLISH)
  )
  new RbOnPullRequest().react(
    new ProjectFarm(project, xpath),
    github,
    Json.createObjectBuilder().add(
        "pull_request",
        Json.createObjectBuilder()
            .add("number", pull.number())
    ).add(
      "repository",
      Json.createObjectBuilder()
        .add("full_name", repo.coordinates().toString())
    ).build()
  )
}

// @todo #69:15min We're now using this class here and
//  adds_new_pull_request_to_wbs/_before.groovy. In the future we might have
//  even more uses after we've expanded the bundles tests. Probably better to
//  factor this out into its own class file in src/test/java.
class ProjectFarm implements Farm {

  private final Project proj
  private final String xpath

  ProjectFarm(final Project proj, final String xpath) {
    this.proj = proj
    this.xpath = xpath
  }

  @Override
  Iterable<Project> find(final String xpath) throws IOException {
    if (this.xpath == xpath) {
      return new IterableOf<Project>(this.proj)
    } else {
      return Collections.emptyList()
    }
  }
}