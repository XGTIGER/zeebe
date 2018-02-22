/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.util.sched;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.zeebe.util.sched.testing.ActorSchedulerRule;
import org.junit.Rule;
import org.junit.Test;

public class TimerExecutionTest
{
    @Rule
    public final ActorSchedulerRule schedulerRule = new ActorSchedulerRule(3);

    @Test
    public void testRunDelayed() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(100_000);

        for (int i = 0; i < 100_000; i++)
        {
            schedulerRule.submitActor(new ZbActor()
            {
                @Override
                protected void onActorStarted()
                {
                    actor.runDelayed(Duration.ofMillis(500), this::timedMethod);
                }

                void timedMethod()
                {
                    latch.countDown();
                }
            });
        }

        if (!latch.await(5, TimeUnit.MINUTES))
        {
            fail("onActorStarted() never called");
        }

        schedulerRule.get().dumpMetrics(System.out);
    }

    @Test
    public void testScheduleAtFixedRate() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(10);

        schedulerRule.submitActor(new ZbActor()
        {
            @Override
            protected void onActorStarted()
            {
                actor.runAtFixedRate(Duration.ofMillis(1), this::timedMethod);
            }

            void timedMethod()
            {
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.MINUTES))
        {
            fail("onActorStarted() never called");
        }
    }

    @Test
    public void testScheduleAtFixedRateConcurrent() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1_000_000);

        for (int i = 0; i < 100_000; i++)
        {
            schedulerRule.submitActor(new ZbActor()
            {
                @Override
                protected void onActorStarted()
                {
                    actor.runAtFixedRate(Duration.ofMillis(1), this::timedMethod);
                }

                void timedMethod()
                {
                    latch.countDown();
                }
            });
        }

        if (!latch.await(5, TimeUnit.MINUTES))
        {
            fail("onActorStarted() never called");
        }

        schedulerRule.get().dumpMetrics(System.out);
    }

    @Test
    public void testCancelRunDelayed() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isInvoked = new AtomicBoolean();

        schedulerRule.submitActor(new ZbActor()
        {
            @Override
            protected void onActorStarted()
            {
                final ScheduledTimer subscription = actor.runDelayed(Duration.ofMillis(100), this::timedMethod);
                subscription.cancel();

                actor.runDelayed(Duration.ofMillis(500), () -> latch.countDown());
            }

            void timedMethod()
            {
                isInvoked.set(true);
            }
        });

        if (!latch.await(5, TimeUnit.MINUTES))
        {
            fail("onActorStarted() never called");
        }

        assertThat(isInvoked).isFalse();
    }

    @Test
    public void testCancelRunAtFixedRate() throws InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger invocations = new AtomicInteger(0);

        schedulerRule.submitActor(new ZbActor()
        {
            ScheduledTimer subscription;

            @Override
            protected void onActorStarted()
            {
                subscription = actor.runDelayed(Duration.ofMillis(100), this::timedMethod);

                actor.runDelayed(Duration.ofMillis(500), () -> latch.countDown());
            }

            void timedMethod()
            {
                invocations.incrementAndGet();
                subscription.cancel();
            }
        });

        if (!latch.await(5, TimeUnit.MINUTES))
        {
            fail("onActorStarted() never called");
        }

        assertThat(invocations.get()).isEqualTo(1);
    }

}
