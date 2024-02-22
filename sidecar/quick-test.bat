curl -X POST http://localhost:8500/kafka/events/camunda/publishing --data-raw "{\"workflowId\": \"da2fd17b-0d0f-4d00-b88e-13d0361073c1\",\"taskId\": \"7055fb17-b008-4142-98f2-dcf9f4d13dc2\", \"feedbackRequired\": true, \"feedback\":{\"feedbackEvent\":\"AuthorizedByOtherDevice\",\"feedbackType\":\"SIGNAL\"},\"data\": {\"author\":\"gandelfwiz\"}}" -H "Content-Type: application/json"

curl -X POST http://localhost:8500/kafka/events/feedback/publishing --data-raw "{\"workflowId\": \"7b17db81-d0d0-11ee-a84d-581cf8936878\",\"taskId\": \"7055fb17-b008-4142-98f2-dcf9f4d13dc2\",\"result\": \"OK\",\"timestamp\": \"2023-01-23T01:03:10\",\"componentName\":\"curl\",\"feedback\":{\"feedbackEvent\":\"AuthorizedByOtherDevice\",\"feedbackType\":\"MESSAGE\"},\"data\": {\"author\": \"gandelfwiz\"}}" -H "Content-Type: application/json"