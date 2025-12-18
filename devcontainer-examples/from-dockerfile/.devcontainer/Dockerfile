FROM debian:trixie-slim

# Setup to avoid below issue
# debconf: (TERM is not set, so the dialog frontend is not usable.)
ENV DEBIAN_FRONTEND=noninteractive
ENV DEFAULT_USER=anazcom

# Debian Slim Images creates a file called docker 
# to keep certain files away of the image such 
# as man pages. Removing it to be able to 
# have that in the image
RUN rm -f /etc/dpkg/dpkg.cfg.d/docker && \
    apt-get update && \
    apt-get upgrade && \
    apt-get install -y \
    man-db \ 
    manpages \
    manpages-dev \
    git \
    bash-completion \
    make \
    starship \
    curl \
    jq \
    vim

RUN useradd -ms /bin/bash $DEFAULT_USER &&\
    mkdir -p /workspaces &&\
    chown $DEFAULT_USER:$DEFAULT_USER /workspaces

USER $DEFAULT_USER
WORKDIR /workspaces

#RUN git clone https://github.com/anazcom/dotfiles.git /tmp/dotfiles &&\
#    /tmp/dotfiles/install.sh
