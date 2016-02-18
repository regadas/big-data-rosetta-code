/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.bdrc

import com.twitter.scalding.TypedPipe

import scala.util.Try

/**
 * Filter out messy data that may have nulls or empty collections
 */
object FilterMessyData {

  case class MessyData(user: String, gender: String, scores: Array[Double], favorites: Set[String])

  /** Dummy method that may fail for invalid records. */
  def compute(x: MessyData): String = "dummy_result"

  /** Naive approach that requires checking every field accessed. */
  def scaldingNaive(input: TypedPipe[MessyData]): TypedPipe[String] = {
    input
      .filter { x =>
        x.user != null && x.gender != null &&
          x.scores != null && x.scores.nonEmpty &&
          x.favorites != null && x.favorites.nonEmpty
      }
      .map(compute)
  }

  /**
   * Smart approach that throws any failed records away.
   *
   * Try.toOption returns Some if the computation succeeds or None if it fails.
   * Option.toSeq is required since .flatMap expects T => TraversableOnce.
   */
  def scalding(input: TypedPipe[MessyData]): TypedPipe[String] = {
    input
      .flatMap(x => Try(compute(x)).toOption.toSeq)
  }

}
