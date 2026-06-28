# build static site
FROM node:lts-alpine AS build
# copy project files and folders to the current working directory (i.e. 'src' folder)
COPY ./package*.json .
# install project dependencies
RUN npm clean-install --no-fund --ignore-scripts --no-audit
COPY . .
# build app for production with minification
RUN npm run build

# serve static site using nginx
FROM nginx:stable-alpine AS serve
COPY --from=build ./dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/nginx.conf
COPY ./entrypoint.sh /entrypoint.sh

RUN chmod +x ./entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/entrypoint.sh"]
