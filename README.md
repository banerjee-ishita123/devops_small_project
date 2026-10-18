# Small End-to-End DevOps Project

A small Java web app that goes from a `git push` to a running Kubernetes deployment with no manual steps.

- GitHub repo: https://github.com/banerjee-ishita123/devops_small_project
- Docker Hub image: `ishitabanerjee/devops-app`

## Architecture

```
Developer
   |  git push
   v
GitHub  --webhook-->  Jenkins (EC2)
                         |
                         |  1. Maven: build + test
                         |  2. Docker: build image (tag = build number)
                         |  3. Push image to Docker Hub
                         |  4. Deploy over SSH: kubectl apply
                         v
                 App server (EC2) - KIND Kubernetes cluster
                         |
                         v
                 http://<app-server-ip>:30080
```

Infrastructure is created by Terraform and configured by Ansible.

## Tools and what each one does

| Tool | Role in this project |
|---|---|
| Terraform | Creates the AWS EC2 servers, security group and key pair |
| Ansible | Installs Java, Maven, Docker and Jenkins on the Jenkins server, and Docker on the app server |
| GitHub | Stores code, pipeline, Kubernetes and infrastructure files; sends a webhook on every push |
| Maven | Compiles, runs the unit test and packages the jar |
| Jenkins | Runs the CI/CD pipeline (declarative Jenkinsfile) |
| Docker | Packages the app into an image |
| Docker Hub | Stores the images, one tag per build number |
| Kubernetes (KIND) | Runs 2 replicas, rolling updates and self-healing |
| AWS | EC2 (servers), security groups (firewall), key pair (SSH login) |

## Repository layout

```
.
├── app/            Java app, pom.xml, Dockerfile
├── k8s/            Deployment and Service YAML
├── terraform/      AWS infrastructure
├── ansible/        Server configuration playbooks
└── Jenkinsfile     CI/CD pipeline
```

## Pipeline stages

1. Checkout the code from GitHub
2. Maven build and unit test (a failing test stops the pipeline)
3. Docker build, tagged with the Jenkins build number
4. Push the image to Docker Hub (credentials stored in Jenkins)
5. Deploy to Kubernetes over SSH and wait for the rollout to finish

## Key features

- Fully automatic: a push to `main` starts the pipeline through a GitHub webhook
- Every build has its own image tag, so any version can be traced and rolled back
- Rolling updates with a readiness probe, so there is no downtime
- Rollback with one command: `kubectl rollout undo deployment/devops-app`
- No passwords in code: Docker Hub token and SSH key are stored as Jenkins credentials
- Terraform state files, keys and inventory are kept out of Git with `.gitignore`

## How to run it

1. `cd terraform && terraform init && terraform apply`
2. Put the server IPs into `ansible/inventory.ini`, then run both playbooks
3. In Jenkins, add the credentials `dockerhub-creds` and `app-server-ssh`, and the global variable `APP_SERVER_IP`
4. Create a Pipeline job from this repo, and add a GitHub webhook pointing to `http://<jenkins-ip>:8080/github-webhook/`
5. Push a change, then open `http://<app-server-ip>:30080`

## What I would improve for production

- Store Terraform state in S3 with locking
- Use EKS instead of KIND
- Use IAM roles instead of access keys
- Add image vulnerability scanning
- Add a webhook secret, HTTPS and restricted ports
- Separate dev and prod environments, with an approval step before prod
