import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

type Job = {
    jobId: number;
    orderId: number;
    jobType: string;
    status: string;
    message: string;
    updatedAt: string;
};

function BackgroundJobMonitor() {

    const [jobs, setJobs] = useState<Job[]>([]);

    useEffect(() => {

        // ================================
        // LOAD EXISTING JOBS FROM BACKEND
        // ================================

        fetch("http://localhost:8081/api/jobs")
            .then((response) => {

                if (!response.ok) {
                    throw new Error(
                        "Failed to fetch background jobs"
                    );
                }

                return response.json();
            })

            .then((data: Job[]) => {

                console.log(
                    "Background jobs:",
                    data
                );

                setJobs(data);
            })

            .catch((error) => {

                console.error(
                    "Error loading background jobs:",
                    error
                );

            });


        // ================================
        // WEBSOCKET CONNECTION
        // ================================

        const client = new Client({

            brokerURL:
                "ws://localhost:8081/ws-smartcart",

            reconnectDelay: 5000,

            onConnect: () => {

                console.log(
                    "Connected to SmartCart WebSocket"
                );


                client.subscribe(
                    "/topic/jobs",
                    (message) => {

                        try {

                            const updatedJob: Job =
                                JSON.parse(message.body);


                            console.log(
                                "Job update received:",
                                updatedJob
                            );


                            setJobs((oldJobs) => {

                                const exists =
                                    oldJobs.some(
                                        (job) =>
                                            job.jobId ===
                                            updatedJob.jobId
                                    );


                                if (exists) {

                                    return oldJobs.map(
                                        (job) =>
                                            job.jobId ===
                                            updatedJob.jobId
                                                ? updatedJob
                                                : job
                                    );

                                }


                                return [
                                    updatedJob,
                                    ...oldJobs
                                ];

                            });

                        } catch (error) {

                            console.error(
                                "Invalid WebSocket message:",
                                error
                            );

                        }

                    }
                );

            },

            onStompError: (frame) => {

                console.error(
                    "STOMP error:",
                    frame
                );

            },

            onWebSocketError: (error) => {

                console.error(
                    "WebSocket error:",
                    error
                );

            }

        });


        // Start WebSocket
        client.activate();


        // Cleanup
        return () => {

            client.deactivate();

        };

    }, []);


    return (

        <div className="job-monitor">

            <h2>
                Background Job Monitor
            </h2>


            <table>

                <thead>

                    <tr>

                        <th>
                            Job ID
                        </th>

                        <th>
                            Order ID
                        </th>

                        <th>
                            Task
                        </th>

                        <th>
                            Status
                        </th>

                        <th>
                            Message
                        </th>

                    </tr>

                </thead>


                <tbody>

                    {jobs.length === 0 ? (

                        <tr>

                            <td
                                colSpan={5}
                                style={{
                                    textAlign: "center"
                                }}
                            >
                                No background jobs found
                            </td>

                        </tr>

                    ) : (

                        jobs.map((job) => (

                            <tr
                                key={job.jobId}
                            >

                                <td>
                                    {job.jobId}
                                </td>

                                <td>
                                    {job.orderId}
                                </td>

                                <td>
                                    {job.jobType}
                                </td>

                                <td
                                    className={
                                        job.status.toLowerCase()
                                    }
                                >
                                    {job.status}
                                </td>

                                <td>
                                    {job.message}
                                </td>

                            </tr>

                        ))

                    )}

                </tbody>

            </table>

        </div>

    );
}

export default BackgroundJobMonitor;